package com.commit.camp_reserve.dto;

import lombok.*;

import java.util.Date;

@Getter
@Builder
public class CampingFacilitiesDto {
    private int campFacsId;       // 편의시설 식별키
    private int campId;           // 캠핑장 식별키
    private int facsTypeId;       // 시설 유형 식별키
    private String innerFacsTy;   // 내부시설 유형
    private int toiletCo;         // 화장실 개수
    private int swrmCo;           // 샤워실 개수
    private int wtrplCo;          // 개수대 개수
    private int brazierCo;        // 화로대 개수
    private String options;       // 부대시설
    private String posblFcltCl;   // 주변이용가능시설
    private String animalCmgCl;   // 애완동물 출입
    private String firstImageUrl;   // 대표 이미지 URL
    private Date createdTime;     // 등록일
    private Date modifiedTime;    // 수정일
    private String operDeCl;      // 운영일
    private String trlerAcmpnyAt; // 개인 트레일러 동반 여부
    private String caravAcmpnyAt; // 개인 카라반 동반 여부
    private String dgpmnLendCl;   // 대여 가능한 텐트 리스트
}
