package com.commit.campus.controller;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.entity.Reservation;
import com.commit.campus.service.ReservationService;
import com.commit.campus.view.ReservationRequest;
import com.commit.campus.view.ReservationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1/reservations")
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/redis_check")
    public String redisCheck() {
        return reservationService.redisHealthCheck();
    }

    // 예약 등록
    @PostMapping("/create")
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequest reservationRequest) throws ParseException {

        /* 질문.
            코드 리뷰 중 RequestBody에 reservationDTO를 쓰지 않고 reservationRequest를 따로 생성해서 사용하는 이유가 있는지 질문이 들어왔는데,
            request는 사용자가 전달해 주는 정보만 따로 담을 용도로 생성하였고,
            reservationDTO에는 request 정보 + 예약 내역 관리를 위해 저장해야할 내용을 추가적으로 작성하였습니다.

            이처럼 requestBody에 DTO를 사용하지 않고 별도의 클래스를 정의하여 사용해도 괜찮은지 궁금합니다.
        */
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

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date entryDate = formatter.parse(reservationRequest.getEntryDate());
        Date leavingDate = formatter.parse(reservationRequest.getLeavingDate());

        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setUserId(Long.valueOf(reservationRequest.getUserId()));
        reservationDTO.setCampId(reservationRequest.getCampId());
        reservationDTO.setCampFacsId(reservationRequest.getCampFacsId());
        reservationDTO.setReservationDate(reservationDate);
        reservationDTO.setEntryDate(entryDate);
        reservationDTO.setLeavingDate(leavingDate);
        reservationDTO.setGearRentalStatus(reservationRequest.getGearRentalStatus());

        return reservationDTO;
    }
}
