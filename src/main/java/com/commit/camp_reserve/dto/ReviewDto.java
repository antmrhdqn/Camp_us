package com.commit.camp_reserve.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long campId;
    private String userId;
    private String reviewContent;
    private Integer rating;
    private String reviewCreatedDate;
    private String reviewModifiedDate;
    private String reviewImageUrl;
}
