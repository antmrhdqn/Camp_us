package com.commit.camp_reserve.entity;

import lombok.Getter;
import jakarta.persistence.*;

@Entity
@Getter
public class Camping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campId;  // 캠핑장 ID

    private String factNm;  // 시설 이름
    private String lineIntro;  // 한 줄 소개
    private String intro;  // 소개
    private String doNm;  // 도 이름
    private String sigunguNm;  // 시군구 이름
    private String zipcode;  // 우편번호
    private String addr1;  // 주소1
    private String addr2;  // 주소2
    private double mapX;  // 지도 X 좌표
    private double mapY;  // 지도 Y 좌표
    private String tel;  // 전화번호
    private String homepage;  // 홈페이지
    private String manageNmpr;  // 관리 인원
}
