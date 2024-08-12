package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.entity.Reservation;
import com.commit.campus.entity.Availability;
import com.commit.campus.entity.Camping;
import com.commit.campus.repository.AvailabilityRepository;
import com.commit.campus.repository.CampingRepository;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final CampingRepository campingRepository;
    private final RedisCommands<String, String> redisCommands;

    int index = 1;  // reservationId 생성용 인덱스
    private static final int CHANGE_COUNT = 1;
    private static final long DEFAULT_TTL_SECONDS = 7200;
    private static final long LOCK_TIMEOUT_SECONDS = 10;  // 락의 타임아웃 설정
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CONFIRMATION_STATUS = "confirmation";
    private static final String CANCELLED_STATUS = "cancelled";

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  AvailabilityRepository availabilityRepository,
                                  CampingRepository campingRepository,
                                  RedisCommands<String, String> redisCommands) {
        this.reservationRepository = reservationRepository;
        this.availabilityRepository = availabilityRepository;
        this.campingRepository = campingRepository;
        this.redisCommands = redisCommands;
    }

    @Override
    public String createReservation(ReservationDTO reservationDTO) {
        String reservationId = createReservationId(reservationDTO.getReservationDate());
        String key = "reservationInfo:" + reservationId;

        log.info("Redis key = {}", key);

        // 예약 정보를 redis에 저장
        saveToRedis(key, reservationDTO, reservationId);

        return reservationId;
    }

    @Override
    @Transactional
    public ReservationDTO confirmReservation(String reservationId) {
        String lockKey = "lock:reservation:" + reservationId;
        try {
            if (!acquireLock(lockKey)) {
                throw new ConcurrentModificationException("해당 예약은 현재 처리 중입니다. 잠시 후 다시 시도해 주세요.");
            }

            String key = "reservationInfo:" + reservationId;
            Long reservationPk = Long.valueOf(reservationId);
            Map<String, String> reservationInfo = redisCommands.hgetall(key);

            // 예약 요청 만료 여부 판별
            if (reservationInfo.isEmpty()) {
                throw new RuntimeException("이미 만료되었거나 존재하지 않는 예약입니다.");
            }

            // 같은 예약 번호가 들어온 경우
            Optional<Reservation> optionalReservation = reservationRepository.findById(reservationPk);

            if (optionalReservation.isPresent()) {
                String reservationStatus = optionalReservation.get().getReservationStatus();
                if (reservationStatus.equals("예약 취소")) {
                    throw new IllegalStateException("이미 취소된 예약입니다.");
                } else {
                    throw new IllegalStateException("이미 존재하는 예약입니다: " + reservationPk);
                }
            }

            // redis에서 가져온 데이터 잘 들어오는지 확인용
            reservationInfo.forEach((keyCheck, valueCheck) -> log.info("Key: {} / Value: {}", keyCheck, valueCheck));

            // 캐시에서 가져온 데이터를 dto로 매핑
            ReservationDTO reservationDTO = mapToReservationDTO(reservationInfo);

            // 예약 정보 db에 저장
            saveReservationToDatabase(reservationDTO);

            // 예약 가능 개수 차감
            updateAvailability(reservationDTO, true);

            return reservationDTO;
        } finally {
            releaseLock(lockKey);
        }
    }

    @Override
    @Transactional
    public void cancelReservation(ReservationDTO reservationDTO) {
        String lockKey = "lock:reservation:" + reservationDTO.getReservationId();
        try {
            if (!acquireLock(lockKey)) {
                throw new RuntimeException("해당 예약은 현재 처리 중입니다. 잠시 후 다시 시도해 주세요.");
            }

            // 예약 취소 로직 실행
            Reservation reservation = reservationRepository.findById(reservationDTO.getReservationId()).orElse(null);
            if (reservation == null) {
                throw new IllegalArgumentException("해당 예약이 존재하지 않습니다.");
            }

            updateCancellationInfo(reservation, CANCELLED_STATUS);

            updateAvailability(reservationDTO, false);
        } finally {
            releaseLock(lockKey);
        }
    }

    /* 예약 등록 */
    // 예약아이디 생성 (예약날짜 + 인덱스)
    private String createReservationId(LocalDateTime reservationDate) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String formattedDate = reservationDate.format(dateFormat);
        String indexCode = String.format("%06d", index++);
        return formattedDate + indexCode;
    }

    // 예약 정보를 redis에 저장
    private void saveToRedis(String key, ReservationDTO reservationDTO, String reservationId) {
        redisCommands.expire(key, DEFAULT_TTL_SECONDS);
        redisCommands.hset(key, "reservationId", reservationId);
        redisCommands.hset(key, "userId", reservationDTO.getUserId().toString());
        redisCommands.hset(key, "campId", reservationDTO.getCampId().toString());
        redisCommands.hset(key, "campFacsId", reservationDTO.getCampFacsId().toString());
        redisCommands.hset(key, "reservationDate", reservationDTO.getReservationDate().toString());
        redisCommands.hset(key, "entryDate", reservationDTO.getEntryDate().toString());
        redisCommands.hset(key, "leavingDate", reservationDTO.getLeavingDate().toString());
        redisCommands.hset(key, "gearRentalStatus", reservationDTO.getGearRentalStatus());
        redisCommands.hset(key, "campFacsType", reservationDTO.getCampFacsType().toString());
    }

    /* 예약 확정 */
    private ReservationDTO mapToReservationDTO(Map<String, String> reservationInfo) {
        return ReservationDTO.builder()
                .reservationId(Long.valueOf(reservationInfo.get("reservationId")))
                .userId(Long.valueOf(reservationInfo.get("userId")))
                .campId(Long.valueOf(reservationInfo.get("campId")))
                .campFacsId(Long.valueOf(reservationInfo.get("campFacsId")))
                .reservationDate(LocalDateTime.parse(reservationInfo.get("reservationDate")))
                .entryDate(LocalDate.parse(reservationInfo.get("entryDate"), DATE_FORMAT))
                .leavingDate(LocalDate.parse(reservationInfo.get("leavingDate"), DATE_FORMAT))
                .reservationStatus(CONFIRMATION_STATUS)
                .gearRentalStatus(reservationInfo.get("gearRentalStatus"))
                .campFacsType(Integer.valueOf(reservationInfo.get("campFacsType")))
                .build();
    }

    private void saveReservationToDatabase(ReservationDTO reservationDTO) {
        Reservation reservation = Reservation.builder()
                .reservationId(reservationDTO.getReservationId())
                .campId(reservationDTO.getCampId())
                .campFacsId(reservationDTO.getCampFacsId())
                .userId(reservationDTO.getUserId())
                .reservationDate(reservationDTO.getReservationDate())
                .entryDate(reservationDTO.getEntryDate())
                .leavingDate(reservationDTO.getLeavingDate())
                .reservationStatus(reservationDTO.getReservationStatus())
                .gearRentalStatus(reservationDTO.getGearRentalStatus())
                .createdAt(LocalDateTime.now())
                .build();
        reservationRepository.save(reservation);
    }

    // 예약 가능 테이블 업데이트(데이터 추가, 이용가능 개수 변경)
    private void updateAvailability(ReservationDTO reservationDTO, boolean isDecrease) {
        LocalDate currentDate = reservationDTO.getEntryDate();
        LocalDate endDate = reservationDTO.getLeavingDate();

        // 예약한 캠핑장의 입실날짜 ~ 퇴실날짜의 예약 가능 현황 가져오기
        List<Availability> availabilityList = availabilityRepository.findByCampIdAndDateBetween(
                reservationDTO.getCampId(),
                reservationDTO.getEntryDate(),
                reservationDTO.getLeavingDate());
        log.info("findByCampIdAndDateBetween 실행됨");
        log.info("availabilityList = {}", availabilityList);

        int index = 0;

        // availability의 date 컬럼에 입실일자 ~ 퇴실일자 정보가 있는지 점검
        while (!currentDate.isAfter(endDate)) {
            log.info("while문 동작 중: " + index);
            index++;
            // 해당 날짜의 데이터가 availability 테이블에 있는지 판별
            Availability availability = checkAvailabilityDate(currentDate, availabilityList);

            // 들어온 데이터가 없다면 해당 날짜의 데이터가 존재하지 않으므로 새로 생성
            if (availability == null) {
                log.info("일치하는 데이터 없음");
                long campId = reservationDTO.getCampId();
                availability = createAvailability(campId, currentDate);
                log.info("{} 날짜로 예약 가능 현황 생성", currentDate);
            }

            if (isDecrease) {
                updateAvailabilityCount(reservationDTO, availability, -CHANGE_COUNT);
            } else {
                updateAvailabilityCount(reservationDTO, availability, CHANGE_COUNT);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    // 예약 가능 여부 판별
    private Availability checkAvailabilityDate(LocalDate currentDate, List<Availability> availabilityList) {
        log.info("checkAvailabilityDate 실행됨");

        String currentDateStr = DATE_FORMAT.format(currentDate);
        log.info("currentDateStr = {}", currentDateStr);

        // 스트림을 사용하여 조건을 확인하고 로깅
        Availability availability = availabilityList.stream()
                .filter(avail -> DATE_FORMAT.format(avail.getDate()).equals(currentDateStr))
                .peek(avail -> log.info("Checking date: " + DATE_FORMAT.format(avail.getDate())))
                .findFirst()
                .orElse(null);

        log.info("avail = {}", availability);

        return availability;
    }

    private Availability createAvailability(long campId, LocalDate availDate) {
        Camping camping = campingRepository.findById(campId).orElse(null);

        if (camping == null) {
            throw new NullPointerException("해당 campId는 존재하지 않습니다.");
        }

        Availability newAvailability = Availability.builder()
                .campId(campId)
                .date(availDate)
                .generalSiteAvail(camping.getGeneralSiteCnt())
                .carSiteAvail(camping.getCarSiteCnt())
                .glampingSiteAvail(camping.getGlampingSiteCnt())
                .caravanSiteAvail(camping.getCaravanSiteCnt())
                .build();

        log.info("createAvailability 실행 완료");

        availabilityRepository.save(newAvailability);
        log.info("newAvailability = {}", newAvailability);

        return newAvailability;
    }

    private void updateAvailabilityCount(ReservationDTO reservationDTO, Availability availability, int changeCount) {
        int campFacsType = reservationDTO.getCampFacsType();

        switch (campFacsType) {
            case 1:
                availability = availability.toBuilder()
                        .generalSiteAvail(availability.getGeneralSiteAvail() + changeCount)
                        .build();
                break;

            case 2:
                availability = availability.toBuilder()
                        .carSiteAvail(availability.getCarSiteAvail() + changeCount)
                        .build();
                break;

            case 3:
                availability = availability.toBuilder()
                        .glampingSiteAvail(availability.getGlampingSiteAvail() + changeCount)
                        .build();
                break;

            case 4:
                availability = availability.toBuilder()
                        .caravanSiteAvail(availability.getCaravanSiteAvail() + changeCount)
                        .build();
                break;

            default:
                throw new IllegalArgumentException("잘못된 시설 유형입니다. : " + campFacsType);
        }

        log.info("카운트 변경됨");
        log.info("availability = {}", availability);

        availabilityRepository.save(availability);
    }

    /* 예약 취소 */
    private void updateCancellationInfo(Reservation reservation, String reservationStatus) {
        if (reservation.getReservationStatus().equals(reservationStatus)) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        Reservation updatedReservation = reservation.toBuilder()
                .reservationStatus(reservationStatus)
                .updatedAt(LocalDateTime.now())
                .build();

        reservationRepository.save(updatedReservation);
        log.info("updatedReservation {}", updatedReservation);
    }

    // Redis 락 획득
    private boolean acquireLock(String lockKey) {
        return Boolean.TRUE.equals(
                redisCommands.set(
                        lockKey, "locked", SetArgs.Builder.nx().ex(LOCK_TIMEOUT_SECONDS)
                )
        );
    }

    private void releaseLock(String lockKey) {
        redisCommands.del(lockKey);
    }
}