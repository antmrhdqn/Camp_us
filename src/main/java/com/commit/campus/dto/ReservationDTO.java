package com.commit.campus.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
public class ReservationDTO {

    private Long reservationId;
    private Long userId;
    private Long campId;
    private Long campFacsId;
    private LocalDateTime reservationDate;
    private LocalDateTime entryDate;
    private LocalDateTime leavingDate;
    private String reservationStatus;
    private String gearRentalStatus;

    private Integer campFacsType;   // 예약한 시설 유형
}
