package com.commit.campus.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long reviewId;
    private Long campId;
    private String reviewContent;
    private int rating;
    private LocalDateTime reviewCreatedDate;
    private LocalDateTime reviewModificationDate;
    private String reviewImageUrl;
}
