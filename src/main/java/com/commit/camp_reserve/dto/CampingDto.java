package com.commit.camp_reserve.dto;

import lombok.*;

@Getter
@Builder
public class CampingDto {
    private int campId;         // 캠핑장 ID
    private String factNm;      // 야영장명
    private String lineIntro;   // 한줄소개
    private String intro;       // 소개
    private String doNm;        // 도
    private String sigunguNm;   // 시군구
    private String zipcode;     // 우편번호
    private String featureNm;   // 특징명
    private String induty;      // 업종
    private String addr1;       // 주소
    private String addr2;       // 주소 상세
    private double mapX;        // 경도
    private double mapY;        // 위도
    private String tel;         // 전화
    private String homepage;    // 홈페이지
    private int manageNmpr;     // 상주관리인원
}
