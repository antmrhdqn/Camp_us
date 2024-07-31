package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RedisClient redisClient;
    private final RedisAsyncCommands redisAsyncCommands;
    private final RedisCommands redisCommands;

    private static long index = 1;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RedisClient redisClient,
                                  RedisAsyncCommands redisAsyncCommands,
                                  RedisCommands redisCommands) {
        this.reservationRepository = reservationRepository;
        this.redisClient = redisClient;
        this.redisAsyncCommands = redisAsyncCommands;
        this.redisCommands = redisCommands;
    }

    @Override
    public String createReservation(ReservationDTO reservationDTO) {

        // 예약 아이디 생성
        LocalDateTime reservationDate = reservationDTO.getReservationDate();
        String reservationId = createReservationId(reservationDate);

        String key = "reservationInfo:" + reservationId;

        /* 궁금증.
            비동기식으로 데이터를 저장한다고 하면, createReservation 요청이 동시에 여러개 날아왔을 때
            redis에 요청 건 별로 데이터가 차곡차곡 저장되는 것이 아니라
            별다른 순서 없이 뒤죽박죽 데이터가 쌓이게 되는 것인지??

            위 가정이 맞다면
            데이터를 찾아올 때에는 해쉬키를 통해서 데이터를 찾아오게 되니 뒤죽박죽 저장되어도 상관 없는 것인지?
        */

        // 동기식으로 데이터 저장
        try {
            redisCommands.hset(key, "reservationId", reservationId);
            redisCommands.hset(key, "user", reservationDTO.getUserId());
            redisCommands.hset(key, "campId", reservationDTO.getCampId());
            redisCommands.hset(key, "campFacsId", reservationDTO.getCampFacsId());
            redisCommands.hset(key, "reservationDate", reservationDTO.getReservationDate());
            redisCommands.hset(key, "entryDate", reservationDTO.getEntryDate());
            redisCommands.hset(key, "leavingDate", reservationDTO.getLeavingDate());
            redisCommands.hset(key, "gearRentalStatus", reservationDTO.getGearRentalStatus());

            log.info("redis에 예약 내역 저장 완료");

        } catch (RuntimeException e) {
            throw new RuntimeException("redis에 저장이 되지 않음");
        }

        return reservationId;
    }

    @Override
    public ReservationDTO confirmReservation(String reservationId) {

//        String key = "reservationInfo:" + reservationId;
//
//        Map<String, String> reservationInfo = redisCommands.hgetall(key);
//
//        if (reservationInfo == null) {
//            return null;
//        }
//
//        reservationInfo.forEach((k, value) -> System.out.println("Key: " + k + ", Value: " + value));

//        ReservationDTO reservationDTO = new ReservationDTO();
//        reservationDTO.setReservationId(reservationId);
//        reservationDTO.setUserId(reservationInfo.get());
//        reservationDTO.setCampId(reservationDTO.getCampId());
//        reservationDTO.setCampFacsId(reservationDTO.getCampFacsId());
//        reservationDTO.setReservationDate(reservationDTO.getReservationDate());
//        reservationDTO.setEntryDate(reservationDTO.getEntryDate());
//        reservationDTO.setLeavingDate(reservationDTO.getLeavingDate());
//        reservationDTO.getGearRentalStatus(reservationDTO.getGearRentalStatus());

        return null;
    }

    // redis에 해쉬 키로 저장할 예약아이디 생성 (예약일자 + 6자리 인덱스값)
    private synchronized String createReservationId(LocalDateTime reservationDate) {

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = reservationDate.format(dateFormat);

        String indexCode = String.format("%06d", index);
        index ++;

        String reservationId = formattedDate + indexCode;

        return reservationId;
    }

    private String generateReservationKey(ReservationDTO reservationDTO) {
        // Generate a unique key based on reservation fields
        return "reservation:" + reservationDTO.getCampFacsId() + ":" + reservationDTO.getUserId();
    }

    private String serializeToJson(ReservationDTO reservationDTO) {
        // Implement JSON serialization (using Jackson or Gson for example)
        return new com.google.gson.Gson().toJson(reservationDTO); // Using Gson for example
    }
}
