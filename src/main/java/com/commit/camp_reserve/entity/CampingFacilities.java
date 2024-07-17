package com.commit.camp_reserve.entity;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
public class CampingFacilities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campFacisId;  // 캠핑장 시설 ID

    @ManyToOne
    @JoinColumn(name = "camp_id")
    private Camping camping;  // 캠핑장 ID

    private String facsTypeId;  // 시설 유형 ID
    private int innerFacs;  // 내부 시설 수
    private int toiletCo;  // 화장실 수
    private int swrmCo;  // 샤워실 수
    private int wtrplCo;  // 식수대 수
    private int brazierCo;  // 화로 수
    private String options;  // 옵션
    private String posblFcltyCl;  // 가능 시설 분류
    private String animalCmgCl;  // 애완동물 출입 가능 여부
    private String firstImageUrl;  // 첫 번째 이미지 URL
    private Date createdTime;  // 생성 시간
    private Date modifiedTime;  // 수정 시간
    private String operDeCl;  // 운영 기간
    private String trlerAcmpnyAt;  // 트레일러 동반 가능 여부
    private String caravAcmpnyAt;  // 카라반 동반 가능 여부
    private String dprtmLendCl;  // 부서 대여 분류
}
