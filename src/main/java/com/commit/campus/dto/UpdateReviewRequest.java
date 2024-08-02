package com.commit.campus.dto;

import lombok.Data;

@Data
public class UpdateReviewRequest {
    private long reviewId;
    private String reviewContent;
    private byte rating;
    private String reviewImageUrl; // TODO : 추후 타입 변경

}
