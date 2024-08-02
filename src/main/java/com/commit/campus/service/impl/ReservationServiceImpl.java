package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RedisTemplate redisTemplate;
    private final RedisCommands redisCommands;

    private static long index = 1;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RedisTemplate redisTemplate,
                                  RedisCommands redisCommands) {
        this.reservationRepository = reservationRepository;
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

        // 예약 아이디 생성
        LocalDateTime reservationDate = reservationDTO.getReservationDate();
        String reservationId = createReservationId(reservationDate);

        String key = "reservationInfo:" + reservationId;

        log.info("key = " + key);

        /* 궁금증.
            - 비동기 방식으로 저장
            redisAsyncCommands를 사용해 비동기식으로 데이터를 redis에 저장할 때,
            요청이 동시에 여러개 날아와도 기본적으로 redis는 단일 스레드이므로 요청이 들어온 순서대로 저장이 이루어진다고 알고 있는데
            그렇다면 동기 방식을 사용해 저장할 때와 어떤 차이가 있는것인지 궁금합니다.
        */

        // 동기식으로 데이터 저장
        try {

            redisCommands.hset(key, "reservationId", reservationId);
            redisCommands.hset(key, "userId", reservationDTO.getUserId().toString());
            redisCommands.hset(key, "campId", reservationDTO.getCampId().toString());
            redisCommands.hset(key, "campFacsId", reservationDTO.getCampFacsId().toString());
            redisCommands.hset(key, "reservationDate", reservationDTO.getReservationDate().toString());
            redisCommands.hset(key, "entryDate", reservationDTO.getEntryDate().toString());
            redisCommands.hset(key, "leavingDate", reservationDTO.getLeavingDate().toString());
            redisCommands.hset(key, "gearRentalStatus", reservationDTO.getGearRentalStatus());

            // 만료 시간 2시간 (= 7200초)
            redisCommands.expire(key, 7200);

        } catch (RuntimeException e) {
            throw new RuntimeException("redis에 저장이 되지 않음");
        }

        return reservationId;
    }

    @Override
    public ReservationDTO confirmReservation(String reservationId) {

        String key = "reservationInfo:" + reservationId;

        log.info("redis key = " + key);

        // 예약 확정 요청이 들어오면 받아온 해시키로 캐시 데이터가 있는지 확인하여 만료 여부 판별
        Map<String, String> reservationInfo = redisCommands.hgetall(key);
        if (reservationInfo.isEmpty()) {
            throw new RuntimeException("이미 만료된 예약입니다.");
        }

        // 데이터 확인용 로그(나중에 삭제)
        for (Map.Entry<String, String> entry : reservationInfo.entrySet()) {
            log.info("Key: " + entry.getKey() + " / Value: " + entry.getValue());
        }

        // 데이터가 있는 경우 rds에 저장하고 예약 가능 건 수 차감, 예약 확정 멘트 보내주기
        ReservationDTO reservationDTO = mapToReservationDTO(reservationInfo);

        // 저장
//        reservationRepository.saveAll(reservationDTO);

        return null;
    }

    // redis에 해쉬 키로 저장할 예약아이디 생성 (예약일자 + 6자리 인덱스값)
    private synchronized String createReservationId(LocalDateTime reservationDate) {

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyMMddhhmmss");
        String formattedDate = reservationDate.format(dateFormat);

        String indexCode = String.format("%06d", index);
        index ++;

        String reservationId = formattedDate + indexCode;

        return reservationId;
    }

    private ReservationDTO mapToReservationDTO(Map<String, String> reservationInfo) {

        // redis에서 꺼내온 데이터 DTO에 매핑 하기

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        ReservationDTO reservationDTO = new ReservationDTO();

        reservationDTO.setReservationId(reservationInfo.get("reservationId"));
        reservationDTO.setUserId(Long.valueOf(reservationInfo.get("userId")));
        reservationDTO.setCampId(Long.valueOf(reservationInfo.get("campId")));
        reservationDTO.setCampFacsId(Long.valueOf(reservationInfo.get("campFacsId")));
        reservationDTO.setReservationDate(LocalDateTime.parse(reservationInfo.get("reservationDate")));
        reservationDTO.setEntryDate(LocalDateTime.parse(reservationInfo.get("entryDate")));
        reservationDTO.setLeavingDate(LocalDateTime.parse(reservationInfo.get("leavingDate")));
        reservationDTO.setReservationStatus("예약 확정");
        reservationDTO.setGearRentalStatus(reservationInfo.get("gearRentalStatus"));

        return reservationDTO;
    }
}
