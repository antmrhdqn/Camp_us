package com.commit.camp_reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacilityTypeDto {
    private int facsTypeId;     // 시설 유형 식별키
    private String facilityName;// 시설명
    private int capacity;       // 수용 가능한 인원
}
