package com.commit.campus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reservations")
public class ReservationController {
    // 예약 관련 기능

    @GetMapping("/")
    public ResponseEntity<Void> reservation() {

        return ResponseEntity.ok().build();
    }
}
