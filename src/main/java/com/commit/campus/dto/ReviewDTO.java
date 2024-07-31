package com.commit.campus.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private long reviewId;
    private long campId;
    private long userId;
    private String userNickname;
    private String reviewContent;
    private byte rating;
    private LocalDateTime reviewCreatedDate;
    private LocalDateTime reviewModificationDate;
    private String reviewImageUrl;
}
