package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RedisClient redisClient;
    private final RedisAsyncCommands redisAsyncCommands;

    private static long index = 1;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RedisClient redisClient,
                                  RedisAsyncCommands redisAsyncCommands) {
        this.reservationRepository = reservationRepository;
        this.redisClient = redisClient;
        this.redisAsyncCommands = redisAsyncCommands;
    }

    @Override
    public void createReservation(ReservationDTO reservationDTO) {

        // 예약 아이디 생성
        LocalDateTime reservationDate = reservationDTO.getReservationDate();
        String reservationId = createReservationId(reservationDate);

        String key = "reservation:" + reservationId;

        // 저장 실패한 경우 예외 처리 추가하기
        redisAsyncCommands.hset(key, "reservationId", reservationId);
        redisAsyncCommands.hset(key, "user", reservationDTO.getUserId());
        redisAsyncCommands.hset(key, "campId", reservationDTO.getCampId());
        redisAsyncCommands.hset(key, "campFacsId", reservationDTO.getCampFacsId());
        redisAsyncCommands.hset(key, "reservationDate", reservationDTO.getReservationDate());
        redisAsyncCommands.hset(key, "entryDate", reservationDTO.getEntryDate());
        redisAsyncCommands.hset(key, "leavingDate", reservationDTO.getLeavingDate());
        redisAsyncCommands.hset(key, "gearRentalStatus", reservationDTO.getGearRentalStatus());

        log.info("redis에 예약 내역 저장 완료");
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
