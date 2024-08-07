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
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final CampingRepository campingRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCommands<String, String> redisCommands;

    private static final AtomicLong index = new AtomicLong(1);
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

        if (reservationInfo.isEmpty()) {
            throw new RuntimeException("이미 만료된 예약입니다.");
        }

        // 데이터 잘 들어오는지 확인용
        reservationInfo.forEach((keyCheck, valueCheck) -> log.info("Key: {} / Value: {}", keyCheck, valueCheck));

        // 캐시에서 가져온 데이터를 dto로 매핑
        ReservationDTO reservationDTO = mapToReservationDTO(reservationInfo);

        // 예약 정보 db에 저장
        saveReservationToDatabase(reservationDTO);

        // 예약 가능 개수 차감
        updateAvailability(reservationDTO);

        return reservationDTO;
    }

    @Override
    @Transactional
    public void cancelReservation(ReservationDTO reservationDTO) {
        Reservation reservation = findReservationById(reservationDTO.getReservationId());
        updateReservationStatus(reservation, "취소");
        updateAvailabilityForCancellation(reservationDTO, reservation);
    }

    private String createReservationId(LocalDateTime reservationDate) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String formattedDate = reservationDate.format(dateFormat);
        String indexCode = String.format("%06d", index.getAndIncrement());
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
                .build();
        reservationRepository.save(reservation);
    }

    private void updateAvailability(ReservationDTO reservationDTO) {
        Date entryDate = Date.from(reservationDTO.getEntryDate().atZone(ZoneId.systemDefault()).toInstant());
        log.info("entryDate = {}", entryDate);

        Date leavingDate = Date.from(reservationDTO.getLeavingDate().atZone(ZoneId.systemDefault()).toInstant());
        log.info("leavingDate = {}", leavingDate);

        // 예약한 캠핑장의 입실날짜 ~ 퇴실날짜의 예약 가능 현황 가져오기
        List<Availability> availabilityList = availabilityRepository.findByCampIdAndDateBetween(
                reservationDTO.getCampId(), entryDate, leavingDate);
        log.info("findByCampIdAndDateBetween 실행됨");
        log.info("availabilityList = {}", availabilityList);

        LocalDate currentDate = entryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = leavingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        int index = 0;
        // availability의 date 컬럼에 입실일자 ~ 퇴실일자 정보가 있는지 점검
        while (!currentDate.isAfter(endDate)) {
            log.info("while문 동작 중: " + index);
            index++;

            // LocalDate -> Date로 변환
            Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // 해당 날짜의 데이터가 availability 테이블에 있는지 판별
            checkAvailabilityDate(date, availabilityList);
//            updateOrCreateAvailability(reservationDTO, date, availabilityList);

            currentDate = currentDate.plusDays(1);
        }

//        updateAvailabilityCount(reservationDTO, entryDate, leavingDate, -1);
    }

    private void checkAvailabilityDate(Date date, List<Availability> availabilityList) {

        log.info("checkAvailabilityDate 실행됨");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String dateStr = formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        log.info("targetDateStr = {}", dateStr);

    }

    private void updateOrCreateAvailability(ReservationDTO reservationDTO, Date date, List<Availability> availabilityList) {

        boolean exists = availabilityList.stream()
                .anyMatch(a -> dateEquals(a.getDate(), date));

        if (!exists) {
            Camping camping = campingRepository.findById(reservationDTO.getCampId())
                    .orElseThrow(() -> new RuntimeException("캠핑장 정보를 찾을 수 없습니다."));
            Availability newAvailability = createAvailability(camping, date);
            availabilityRepository.save(newAvailability);
        }
    }

    private Availability createAvailability(Camping camping, Date date) {
        return Availability.builder()
                .campId(camping.getCampId())
                .date(date)
                .generalSiteAvail(camping.getGeneralSiteCnt())
                .carSiteAvail(camping.getCarSiteCnt())
                .glampingSiteAvail(camping.getGlampingSiteCnt())
                .caravanSiteAvail(camping.getCaravanSiteCnt())
                .build();
    }

    private void updateAvailabilityCount(ReservationDTO reservationDTO, Date entryDate, Date leavingDate, int changeCount) {
        Date currentDate = entryDate;

        while (!currentDate.after(leavingDate)) {
            Date date = new Date(currentDate.getTime());
            Availability availability = availabilityRepository.findByCampIdAndDate(reservationDTO.getCampId(), date);

            if (availability != null) {
                updateAvailabilityForType(availability, reservationDTO.getCampFacsType(), changeCount);
                availabilityRepository.save(availability);
            }

            currentDate = Date.from(currentDate.toInstant().plus(1, java.time.temporal.ChronoUnit.DAYS));
        }
    }

    private void updateAvailabilityForType(Availability availability, int campFacsType, int changeCount) {
        Availability updatedAvailability = Availability.builder()
                .availId(availability.getAvailId())
                .campId(availability.getCampId())
                .date(availability.getDate())
                .generalSiteAvail(availability.getGeneralSiteAvail())
                .carSiteAvail(availability.getCarSiteAvail())
                .glampingSiteAvail(availability.getGlampingSiteAvail())
                .caravanSiteAvail(availability.getCaravanSiteAvail())
                .build();

        switch (campFacsType) {
            case 1:
                updatedAvailability = updatedAvailability.toBuilder()
                        .generalSiteAvail(updatedAvailability.getGeneralSiteAvail() + changeCount)
                        .build();
                break;
            case 2:
                updatedAvailability = updatedAvailability.toBuilder()
                        .carSiteAvail(updatedAvailability.getCarSiteAvail() + changeCount)
                        .build();
                break;
            case 3:
                updatedAvailability = updatedAvailability.toBuilder()
                        .glampingSiteAvail(updatedAvailability.getGlampingSiteAvail() + changeCount)
                        .build();
                break;
            case 4:
                updatedAvailability = updatedAvailability.toBuilder()
                        .caravanSiteAvail(updatedAvailability.getCaravanSiteAvail() + changeCount)
                        .build();
                break;
            default:
                throw new IllegalArgumentException("Invalid camp facility type: " + campFacsType);
        }

        availabilityRepository.save(updatedAvailability);
    }

    private boolean dateEquals(Date date1, Date date2) {
        return date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .equals(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));
    }

    private void updateReservationStatus(Reservation reservation, String status) {
        Reservation updatedReservation = reservation.toBuilder()
                .reservationStatus(status)
                .build();
        reservationRepository.save(updatedReservation);
    }

    private void updateAvailabilityForCancellation(ReservationDTO reservationDTO, Reservation reservation) {
        Date entryDate = Date.from(reservation.getEntryDate().atZone(ZoneId.systemDefault()).toInstant());
        Date leavingDate = Date.from(reservation.getLeavingDate().atZone(ZoneId.systemDefault()).toInstant());
        updateAvailabilityCount(reservationDTO, entryDate, leavingDate, 1);
    }
}