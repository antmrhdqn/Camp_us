package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.entity.Availability;
import com.commit.campus.entity.Reservation;
import com.commit.campus.repository.AvailabilityRepository;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final RedisTemplate redisTemplate;
    private final RedisCommands redisCommands;

    private static long index = 1;
    private static final long DEFAULT_TTL_SECONDS = 7200;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  AvailabilityRepository availabilityRepository,
                                  RedisTemplate redisTemplate,
                                  RedisCommands redisCommands) {
        this.reservationRepository = reservationRepository;
        this.availabilityRepository = availabilityRepository;
        this.redisTemplate = redisTemplate;
        this.redisCommands = redisCommands;
    }

    @Override
    public String redisHealthCheck() {
        try {
            ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
            opsForValue.set("health_check", "OK");
            String result = opsForValue.get("health_check");

            if ("OK".equals(result)) {
                return "레디스 실행중 ~,~";
            } else {
                return "레디스 서버 연결 실패!";
            }

        } catch (Exception e) {
            return "연결 실패했따: " + e.getMessage();
        }
    }

    @Override
    public String createReservation(ReservationDTO reservationDTO) {

        // 예약 가능 여부 체크
        if (!isAvailable(reservationDTO)) {
            throw new RuntimeException("해당 날짜에 예약 가능한 사이트가 없습니다.");
        }

        LocalDateTime reservationDate = reservationDTO.getReservationDate();
        String reservationId = createReservationId(reservationDate);

        String key = "reservationInfo:" + reservationId;

        log.info("key = " + key);

        redisCommands.expire(key, DEFAULT_TTL_SECONDS);

        try {
            redisCommands.hset(key, "reservationId", reservationId);
            redisCommands.hset(key, "userId", reservationDTO.getUserId().toString());
            redisCommands.hset(key, "campId", reservationDTO.getCampId().toString());
            redisCommands.hset(key, "campFacsId", reservationDTO.getCampFacsId().toString());
            redisCommands.hset(key, "reservationDate", reservationDTO.getReservationDate().toString());
            redisCommands.hset(key, "entryDate", reservationDTO.getEntryDate().toString());
            redisCommands.hset(key, "leavingDate", reservationDTO.getLeavingDate().toString());
            redisCommands.hset(key, "gearRentalStatus", reservationDTO.getGearRentalStatus());

        } catch (RuntimeException e) {
            throw new RuntimeException("redis에 저장이 되지 않음");
        }

        return reservationId;
    }

    @Override
    public ReservationDTO confirmReservation(String reservationId) {

        String key = "reservationInfo:" + reservationId;

        log.info("redis key = " + key);

        Map<String, String> reservationInfo = redisCommands.hgetall(key);
        if (reservationInfo.isEmpty()) {
            throw new RuntimeException("이미 만료된 예약입니다.");
        }

        for (Map.Entry<String, String> entry : reservationInfo.entrySet()) {
            log.info("Key: " + entry.getKey() + " / Value: " + entry.getValue());
        }

        ReservationDTO reservationDTO = mapToReservationDTO(reservationInfo);

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

        // 이용 가능 개수 차감
        decreaseAvailability(reservationDTO);

        return reservationDTO;
    }

    private synchronized String createReservationId(LocalDateTime reservationDate) {

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMddhhmmss");
        String formattedDate = reservationDate.format(dateFormat);

        String indexCode = String.format("%06d", index);
        index++;

        String reservationId = formattedDate + indexCode;

        return reservationId;
    }

    private ReservationDTO mapToReservationDTO(Map<String, String> reservationInfo) {

        ReservationDTO reservationDTO = new ReservationDTO();

        reservationDTO.setReservationId(Long.valueOf(reservationInfo.get("reservationId")));
        reservationDTO.setUserId(Long.valueOf(reservationInfo.get("userId")));
        reservationDTO.setCampId(Long.valueOf(reservationInfo.get("campId")));
        reservationDTO.setCampFacsId(Long.valueOf(reservationInfo.get("campFacsId")));
        reservationDTO.setReservationDate(LocalDateTime.parse(reservationInfo.get("reservationDate")));
        reservationDTO.setEntryDate(LocalDateTime.parse(reservationInfo.get("entryDate")));
        reservationDTO.setLeavingDate(LocalDateTime.parse(reservationInfo.get("leavingDate")));
        reservationDTO.setReservationStatus("예약 확정");
        reservationDTO.setGearRentalStatus(reservationInfo.get("gearRentalStatus"));
        reservationDTO.setCampFacsType(Integer.valueOf(reservationInfo.get("campFacsType")));

        return reservationDTO;
    }

    // 예약 가능 여부 체크 메소드
    private boolean isAvailable(ReservationDTO reservationDTO) {
        Availability availability = availabilityRepository.findByCampIdAndDate(
                reservationDTO.getCampId(), java.sql.Date.valueOf(reservationDTO.getReservationDate().toLocalDate())
        );
        if (availability == null) {
            return false;
        }

        switch (reservationDTO.getCampFacsType()) {
            case 1: // 일반 사이트
                return availability.getGeneralSiteAvail() > 0;
            case 2: // 자동차 사이트
                return availability.getCarSiteAvail() > 0;
            case 3: // 글램핑 사이트
                return availability.getGlampingSiteAvail() > 0;
            case 4: // 카라반 사이트
                return availability.getCaravanSiteAvail() > 0;
            default:
                return false;
        }
    }

    // 이용 가능 개수 차감 메소드
    private void decreaseAvailability(ReservationDTO reservationDTO) {
        Availability availability = availabilityRepository.findByCampIdAndDate(
                reservationDTO.getCampId(), java.sql.Date.valueOf(reservationDTO.getReservationDate().toLocalDate())
        );

        if (availability != null) {
            switch (reservationDTO.getCampFacsId().intValue()) {
                case 1: // 일반 사이트
                    availability.setGeneralSiteAvail(availability.getGeneralSiteAvail() - 1);
                    break;
                case 2: // 자동차 사이트
                    availability.setCarSiteAvail(availability.getCarSiteAvail() - 1);
                    break;
                case 3: // 글램핑 사이트
                    availability.setGlampingSiteAvail(availability.getGlampingSiteAvail() - 1);
                    break;
                case 4: // 카라반 사이트
                    availability.setCaravanSiteAvail(availability.getCaravanSiteAvail() - 1);
                    break;
                default:
                    throw new RuntimeException("잘못된 캠프 시설 ID입니다.");
            }

            availabilityRepository.save(availability);
        }
    }
}
