package com.commit.campus.dto;

import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime entryDate;
    private LocalDateTime leavingDate;
    private String reservationStatus;
    private String gearRentalStatus;

    private Integer campFacsType;   // 예약한 시설 유형
}
