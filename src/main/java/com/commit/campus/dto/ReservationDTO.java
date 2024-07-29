package com.commit.campus.dto;

import com.commit.campus.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Builder
@ToString
public class ReservationDTO {

    private int userId;
    private long campId;
    private long campFacsId;
    private LocalDateTime reservationDate;
    private Date entryDate;
    private Date leavingDate;
    private String gearRentalStatus;
}
