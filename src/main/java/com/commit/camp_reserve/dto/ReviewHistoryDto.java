package com.commit.camp_reserve.dto;

import lombok.*;

import java.util.Date;

@Getter
@Builder
public class ReviewHistoryDto {
    private int campId;            // 캠핑장 식별키
    private String userId;          // 유저 이메일
    private String reviewContent;   // 리뷰 내용
    private int rating;             // 평점
    private Date reviewCreatedDate; // 리뷰 작성일자
    private Date reviewModifiedDate;// 리뷰 수정일자
    private String reviewImageUrl;  // 리뷰 사진
}
