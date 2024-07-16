package com.commit.camp_reserve.dto;

import lombok.*;

@Getter
@Builder
public class RatingSummaryDto {
    private int campId;         // 캠핑장 식별키
    private int totalRating;    // 평점 합계
    private int countRating;    // 평점 개수
}
