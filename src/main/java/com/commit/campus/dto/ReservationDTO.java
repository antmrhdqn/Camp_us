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

    private String reservationId;
    private Long userId;
    private Long campId;
    private Long campFacsId;
    private LocalDateTime reservationDate;
    private Date entryDate;
    private Date leavingDate;
    private String reservationStatus;
    private String gearRentalStatus;
}
