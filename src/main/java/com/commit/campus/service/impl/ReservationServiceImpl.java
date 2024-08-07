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
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final CampingRepository campingRepository;
    private final RedisTemplate redisTemplate;
    private final RedisCommands redisCommands;

    private static long index = 1;
    private static final long DEFAULT_TTL_SECONDS = 7200;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  AvailabilityRepository availabilityRepository,
                                  CampingRepository campingRepository,
                                  RedisTemplate redisTemplate,
                                  RedisCommands redisCommands) {
        this.reservationRepository = reservationRepository;
        this.availabilityRepository = availabilityRepository;
        this.campingRepository = campingRepository;
        this.redisTemplate = redisTemplate;
        this.redisCommands = redisCommands;
    }

    @Override
    public String createReservation(ReservationDTO reservationDTO) {

        // 예약 가능 여부 체크
//        if (!isAvailable(reservationDTO)) {
//            throw new RuntimeException("해당 날짜에 예약 가능한 사이트가 없습니다.");
//        }

        LocalDateTime reservationDate = reservationDTO.getReservationDate();
        String reservationId = createReservationId(reservationDate);

        String key = "reservationInfo:" + reservationId;

        log.info("key = " + key);

        // 캐시 만료 시간 설정(7200초)
        redisCommands.expire(key, DEFAULT_TTL_SECONDS);

        // redis에 예약 내역 임시 저장
        try {
            redisCommands.hset(key, "reservationId", reservationId);
            redisCommands.hset(key, "userId", reservationDTO.getUserId().toString());
            redisCommands.hset(key, "campId", reservationDTO.getCampId().toString());
            redisCommands.hset(key, "campFacsId", reservationDTO.getCampFacsId().toString());
            redisCommands.hset(key, "reservationDate", reservationDTO.getReservationDate().toString());
            redisCommands.hset(key, "entryDate", reservationDTO.getEntryDate().toString());
            redisCommands.hset(key, "leavingDate", reservationDTO.getLeavingDate().toString());
            redisCommands.hset(key, "gearRentalStatus", reservationDTO.getGearRentalStatus());
            redisCommands.hset(key, "campFacsType", reservationDTO.getCampFacsType().toString());

        } catch (RuntimeException e) {
            throw new RuntimeException("redis에 저장이 되지 않음");
        }

        return reservationId;
    }

    @Override
    @Transactional
    public ReservationDTO confirmReservation(String reservationId) {

        String key = "reservationInfo:" + reservationId;

        log.info("redis key = " + key);

        // redis 데이터가 살아있는지 판별
        Map<String, String> reservationInfo = redisCommands.hgetall(key);
        if (reservationInfo.isEmpty()) {
            throw new RuntimeException("이미 만료된 예약입니다.");
        }

        // 데이터 확인용 로그 출력
        for (Map.Entry<String, String> entry : reservationInfo.entrySet()) {
            log.info("Key: " + entry.getKey() + " / Value: " + entry.getValue());
        }

        // 예약 테이블에 정보 저장
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

        // 요청받은 예약 정보로 availability에 저장된 데이터 확인
        long campId = reservationDTO.getCampId();
        log.info("campId = " + campId);
        Date entryDate = Date.from(reservationDTO.getEntryDate().atZone(ZoneId.systemDefault()).toInstant());
        log.info("entryDate = " + entryDate);
        Date leavingDate = Date.from(reservationDTO.getLeavingDate().atZone(ZoneId.systemDefault()).toInstant());
        log.info("leavingDate = " + leavingDate);

        // 캠핑장 아이디와 예약시 입력한 입퇴실 날짜가 일치하는 데이터를 모두 조회하여 리스트에 담기
        List<Availability> availabilityList = availabilityRepository.findByCampIdAndDateBetween(campId, entryDate, leavingDate);

        LocalDate currentDate = entryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = leavingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 입실 날짜부터 퇴실 날짜까지 반복하며 해당 날짜로 된 date가 존재하는지 확인
        while (!currentDate.isAfter(endDate)) {
            log.info("while문 동작 중");
            Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // equalsDate 잘 찾는지 로그 확인용
            availabilityList.stream()
                    .filter(a -> dateEquals(a.getDate(), date))
                    .forEach(a -> log.info("찾았다!: " + a.toString()));

            boolean exists = availabilityList.stream()
                    .anyMatch(a -> dateEquals(a.getDate(), date));

            // 해당 날짜의 데이터가 없는 경우 새로 생성
            if (!exists) {
                log.info("하나 추가합니다~");
                Camping camping = campingRepository.findById(campId).orElse(null);
                Availability newAvailability = createAvailability(camping, date);
                availabilityRepository.save(newAvailability);
            }
            currentDate = currentDate.plusDays(1);
        }

        // 예약 가능 개수 업데이트
        updateAvailability(reservationDTO, entryDate, leavingDate, -1);

        return reservationDTO;
    }

    @Override
    @Transactional
    public void cancelReservation(ReservationDTO reservationDTO) {
        // 예약 아이디로 예약 테이블에서 데이터 찾아오기
        Reservation reservation = reservationRepository.findById(reservationDTO.getReservationId())
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다: " + reservationDTO.getReservationId()));

        // 해당 데이터의 reservationStatus를 "취소"로 변경
        Reservation updatedReservation = reservation.toBuilder()
                .reservationStatus("취소")
                .build();
        reservationRepository.save(updatedReservation);

        // 캠핑장 id와 예약한 campFacsType을 이용하여 Availability 테이블의 시설 예약 가능 개수를 하나 증가시킴
        Date entryDate = Date.from(updatedReservation.getEntryDate().atZone(ZoneId.systemDefault()).toInstant());
        Date leavingDate = Date.from(updatedReservation.getLeavingDate().atZone(ZoneId.systemDefault()).toInstant());
        updateAvailability(reservationDTO, entryDate, leavingDate, 1);
    }

    // redis에 저장할 해쉬키 생성(예약일자 + 인덱스값)
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

    // 예약 가능 개수 생성 메소드
    private Availability createAvailability(Camping camping, Date date) {
        Availability availability = new Availability();
        availability.setCampId(camping.getCampId());
        availability.setDate(date);
        availability.setGeneralSiteAvail(camping.getGeneralSiteCnt());
        availability.setCarSiteAvail(camping.getCarSiteCnt());
        availability.setGlampingSiteAvail(camping.getGlampingSiteCnt());
        availability.setCaravanSiteAvail(camping.getCaravanSiteCnt());

        return availability;
    }

    // 이용 가능 개수 차감 메소드
    private void updateAvailability(ReservationDTO reservationDTO, Date entryDate, Date leavingDate, int changeCount) {
        Date currentDate = entryDate;
        log.info("decreaseAvailability 실행됨");

        while (!currentDate.after(leavingDate)) {
            Date date = new Date(currentDate.getTime());
            Availability availability = availabilityRepository.findByCampIdAndDate(reservationDTO.getCampId(), date);

            if (availability != null) {
                log.info("availability = " + availability);

                if (availability != null) {
                    log.info("if문 실행됨");
                    switch (reservationDTO.getCampFacsType()) {
                        case 1: // 일반 야영장
                            availability.setGeneralSiteAvail(availability.getGeneralSiteAvail() + changeCount);
                            log.info("일반야영장 개수 변경");
                            break;
                        case 2: // 자동차 야영장
                            availability.setCarSiteAvail(availability.getCarSiteAvail() + changeCount);
                            log.info("자동차야영장 개수 변경");
                            break;
                        case 3: // 글램핑장
                            availability.setGlampingSiteAvail(availability.getGlampingSiteAvail() + changeCount);
                            log.info("글램핑 개수 변경");
                            break;
                        case 4: // 카라반
                            availability.setCaravanSiteAvail(availability.getCaravanSiteAvail() + changeCount);
                            log.info("카라반 개수 변경");
                            break;
                        default:
                            throw new RuntimeException("잘못된 캠프 시설 유형입니다.");
                    }
                    availabilityRepository.save(availability);
                }
            }
            currentDate = addDays(currentDate, 1);
        }
    }

    // 날짜 비교 메소드
    public boolean dateEquals(Date date1, Date date2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr1 = formatter.format(date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        String dateStr2 = formatter.format(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return dateStr1.equals(dateStr2);
    }

    private Date addDays(Date date, int days) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        localDate = localDate.plusDays(days);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
}