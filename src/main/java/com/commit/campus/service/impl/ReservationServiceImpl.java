package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.entity.Availability;
import com.commit.campus.entity.Camping;
import com.commit.campus.entity.Reservation;
import com.commit.campus.repository.AvailabilityRepository;
import com.commit.campus.repository.CampingRepository;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final CampingRepository campingRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCommands<String, String> redisCommands;

    private static final int CHANGE_COUNT = 1;
    private static final long DEFAULT_TTL_SECONDS = 7200;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  AvailabilityRepository availabilityRepository,
                                  CampingRepository campingRepository,
                                  RedisTemplate<String, String> redisTemplate,
                                  RedisCommands<String, String> redisCommands) {
        this.reservationRepository = reservationRepository;
        this.availabilityRepository = availabilityRepository;
        this.campingRepository = campingRepository;
        this.redisTemplate = redisTemplate;
        this.redisCommands = redisCommands;
    }

    @Override
    public String redisHealthCheck() {
        try {
            redisTemplate.opsForValue().set("health_check", "OK");
            String result = redisTemplate.opsForValue().get("health_check");
            return "OK".equals(result) ? "Redis is running" : "Redis connection failed";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
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
        String key = "reservationInfo:" + reservationId;
        Map<String, String> reservationInfo = redisCommands.hgetall(key);

        // 예약 요청 만료 여부 판별
        if (reservationInfo.isEmpty()) {
            throw new RuntimeException("이미 만료된 예약입니다.");
        }

        // redis에서 가져온 데이터 잘 들어오는지 확인용
        reservationInfo.forEach((keyCheck, valueCheck) -> log.info("Key: {} / Value: {}", keyCheck, valueCheck));

        // 캐시에서 가져온 데이터를 dto로 매핑
        ReservationDTO reservationDTO = mapToReservationDTO(reservationInfo);

        // 예약 정보 db에 저장
        saveReservationToDatabase(reservationDTO);

        // 예약 가능 개수 차감
        boolean isIncrement = true;
        updateAvailability(reservationDTO, isIncrement);

        return reservationDTO;
    }

    @Override
    @Transactional
    public void cancelReservation(ReservationDTO reservationDTO) {

        /*
            예약 취소 요청이 들어오면
            1. reservationID로 rds에 저장된 예약 내역을 불러와 reservationStatus를 "취소"로 업데이트
            2. entryDate, leavingDate, campFacsType 값을 저장
            3. availability 테이블에서 위 기간의 예약 가능 카운트를 증가
        */
        Reservation reservation = reservationRepository.findById(reservationDTO.getReservationId()).orElse(null);

        // 예약 내역 변경
        String reservationStatus = "예약 취소";
        updateCancellationInfo(reservation, reservationStatus);

        // 예약 가능 개수 증가
        boolean isIncrement = false;
        updateAvailability(reservationDTO, isIncrement);
    }

    /* 예약 등록 */
    private String createReservationId(LocalDateTime reservationDate) {
        int index = 1;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String formattedDate = reservationDate.format(dateFormat);
        String indexCode = String.format("%06d", index);
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
                .entryDate(LocalDateTime.parse(reservationInfo.get("entryDate")))
                .leavingDate(LocalDateTime.parse(reservationInfo.get("leavingDate")))
                .reservationStatus("예약 확정")
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

    private void updateAvailability(ReservationDTO reservationDTO, boolean isIncrese) {

        Date entryDate = setDate(reservationDTO.getEntryDate());
        Date leavingDate = setDate(reservationDTO.getLeavingDate());
        log.info("entryDate = {}", entryDate);
        log.info("leavingDate = {}", leavingDate);

        // 예약한 캠핑장의 입실날짜 ~ 퇴실날짜의 예약 가능 현황 가져오기
        List<Availability> availabilityList = availabilityRepository.findByCampIdAndDateBetween(
                reservationDTO.getCampId(), entryDate, leavingDate);
        log.info("findByCampIdAndDateBetween 실행됨");
        log.info("availabilityList = {}", availabilityList);

        // date값을 조건문에 사용(isAfter 활용)하기 위해 LocalDate 타입으로 전환
        LocalDate currentDate = setLocalDate(entryDate);
        LocalDate endDate = setLocalDate(leavingDate);

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

            if(isIncrese) {
                updateAvailabilityCount(reservationDTO, availability, -CHANGE_COUNT);
            } else {
                updateAvailabilityCount(reservationDTO, availability, CHANGE_COUNT);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    private Date setDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate setLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Availability checkAvailabilityDate(LocalDate currentDate, List<Availability> availabilityList) {

        /*
            매개변수로 받아온 date는 입실일자 ~ 퇴실 일자부터 하나씩 증가하는 데이터
            availability 테이블에 해당 값들이 존재하는지 체크해야 함.

            1. currentDate를 String 타입으로 Formatting
            2. availability에서 date 값을 꺼내와 Formatting
            3. currentDate와 date를 비교
        */

        log.info("checkAvailabilityDate 실행됨");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDateStr = formatter.format(currentDate);
        log.info("currentDateStr = {}", currentDateStr);

        // 스트림을 사용하여 조건을 확인하고 로깅
        Availability availability = availabilityList.stream()
                .filter(avail -> formatter.format(avail.getDate().toInstant().atZone(ZoneId.systemDefault())).equals(currentDateStr))
                .peek(avail -> log.info("Checking date: " + formatter.format(avail.getDate().toInstant().atZone(ZoneId.systemDefault()))))
                .findFirst()
                .orElse(null);

        log.info("avail = {}", availability);

        return availability;
    }

    private Availability createAvailability(long campId, LocalDate date) {

        Camping camping = campingRepository.findById(campId).orElse(null);

        if (camping == null) {
            throw new NullPointerException("해당 campId는 존재하지 않습니다.");
        }

        // LocalDate -> date 타입 변환
        Date availDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        log.info("availDate = {}", availDate);

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

        /*
            1. 예약한 정보에서 camp id와 facsType을 가져옴
            2. facsType을 체크하여 해당하는 시설의 cnt를 하나 차감
        */

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

        if(reservation.getReservationStatus().equals(reservationStatus)) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        Reservation updatedReservation = reservation.toBuilder()
                .reservationStatus(reservationStatus)
                .updatedAt(LocalDateTime.now())
                .build();

        reservationRepository.save(updatedReservation);
        log.info("updatedReservation {}", updatedReservation);
    }
}