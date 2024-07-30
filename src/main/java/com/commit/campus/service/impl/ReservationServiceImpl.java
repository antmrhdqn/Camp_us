package com.commit.campus.service.impl;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.ReservationService;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private RedisCommands<String, String> redisCommands;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository, RedisCommands<String, String> redisCommands) {
        this.reservationRepository = reservationRepository;
        this.redisCommands = redisCommands;
    }

    @Override
    public void createReservation(ReservationDTO reservationDTO) {
        // Generate a unique key for the reservation
        String reservationKey = generateReservationKey(reservationDTO);

        String reservationValue = serializeToJson(reservationDTO);

        // Save the reservation request to Redis with TTL of 1 hour
        redisCommands.set(reservationKey, reservationValue);
//        redisCommands.expire(reservationKey, 1, TimeUnit.HOURS);
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
