package com.comm1t.campus.entity;

import lombok.Getter;
import jakarta.persistence.*;

@Entity
@Getter
public class RatingSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campId;  // 캠핑장 ID

    private double totalRating;  // 총 평점
    private int countRating;  // 평점 수
}
