package com.commit.campus.view;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@ToString
public class ReservationRequest {
    private int userId;
    private long campId;
    private long campFacsId;
    private Date entryDate;
    private Date leavingDate;
    private String gearRentalStatus;
}
