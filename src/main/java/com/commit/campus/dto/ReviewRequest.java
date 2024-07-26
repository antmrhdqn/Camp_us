package com.commit.campus.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long campId;
    private String reviewContent;
    private int rating;
    private String reviewImageUrl;
}
