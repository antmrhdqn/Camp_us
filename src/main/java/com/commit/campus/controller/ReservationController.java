package com.commit.campus.controller;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.entity.Reservation;
import com.commit.campus.repository.CampingFacilitiesRepository;
import com.commit.campus.service.ReservationService;
import com.commit.campus.request.ReservationRequest;
import com.commit.campus.view.ReservationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/reservations")
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;
    private final CampingFacilitiesRepository campingFacilitiesRepository;

    @Autowired
    public ReservationController(ReservationService reservationService, CampingFacilitiesRepository campingFacilitiesRepository) {
        this.reservationService = reservationService;
        this.campingFacilitiesRepository = campingFacilitiesRepository;
    }

    @GetMapping("/redis_check")
    public String redisCheck() {
        return reservationService.redisHealthCheck();
    }

    // 예약 등록
    @PostMapping("/create")
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequest reservationRequest) throws ParseException {

        ReservationDTO reservationDTO = mapToReservationDTO(reservationRequest);

        String reservationId = reservationService.createReservation(reservationDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservationId);
    }

    // 예약 확정(결제)
    @PostMapping("/confirm")
    public ResponseEntity<ReservationView> finalizeReservation(@RequestParam String reservationId) {

        reservationService.confirmReservation(reservationId);

        return ResponseEntity.ok().build();
    }

    // 예약 변경
    @PutMapping("/change")
    public ResponseEntity<Void> modifyReservation(@RequestBody List<Reservation> reservations) {

        // 예약 변경은 예약자가 입력한 정보만 가능하도록 설정(성함, 연락처, 인원수, 장비대여여부 등)
        // 날짜 or 시설 변경을 원할 시에는 예약 취소후 재 예약

        return ResponseEntity.ok().build();
    }

    // 예약 취소
    @PutMapping("/cancel")
    public ResponseEntity<Void> cancelReservation(@RequestBody List<Reservation> reservations, @RequestHeader("Authorization") String token) {

        // 기존 날짜의 예약 가능 건수 차감한 것을 되돌림
        // 예약 히스토리의 상태값 변경

        return ResponseEntity.ok().build();
    }

    private ReservationDTO mapToReservationDTO(ReservationRequest reservationRequest) throws ParseException {

        LocalDateTime reservationDate = LocalDateTime.now();

        long campFacsId = reservationRequest.getCampFacsId();
        int facsType = campingFacilitiesRepository.findById(campFacsId).get().getFacsTypeId();

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setUserId(Long.valueOf(reservationRequest.getUserId()));
        reservationDTO.setCampId(reservationRequest.getCampId());
        reservationDTO.setCampFacsId(reservationRequest.getCampFacsId());
        reservationDTO.setReservationDate(reservationDate);
        reservationDTO.setEntryDate(LocalDateTime.parse(reservationRequest.getEntryDate()));
        reservationDTO.setLeavingDate(LocalDateTime.parse(reservationRequest.getLeavingDate()));
        reservationDTO.setGearRentalStatus(reservationRequest.getGearRentalStatus());

//        reservationDTO.setCampFacsType();

        return reservationDTO;
    }
}
