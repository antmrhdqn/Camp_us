package com.commit.camp_reserve.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationHistoryDto {
    private String reservationId;
    private String campFacsId;
    private long userId;
    private String userEmail;
    private String reservationDate;
    private String cancelDate;
    private String checkinDate;
    private String checkoutDate;
    private String eqpmnLendCl;

}
