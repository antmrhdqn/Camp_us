package com.commit.campus.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReservationDTO {

    private Long reservationId;
    private Long userId;
    private Long campId;
    private Long campFacsId;
    private LocalDateTime reservationDate;
    private Date entryDate;
    private Date leavingDate;
    private String reservationStatus;
    private String gearRentalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer campFacsType;   // 예약한 시설 유형
}
