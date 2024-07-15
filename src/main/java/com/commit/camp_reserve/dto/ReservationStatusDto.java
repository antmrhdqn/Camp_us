package com.commit.camp_reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationStatusDto {
    private int reservationId;      // 예약 PK
    private Date reservationDate;   // 예약 날짜
    private int currentUsage;       // 현재 이용 가능 개수
    private int totalCapacity;      // 총 개수
    private int campFacilityId;     // 캠핑장 시설 PK
}
