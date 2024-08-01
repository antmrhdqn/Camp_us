package com.commit.campus.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampingDTO {
    private Long campId;
    private String campName;
    private String lineIntro;
    private String intro;
    private String doName;
    private String sigunguName;
    private String postCode;
    private String featureSummary;
    private String induty;
    private String addr;
    private String addrDetails;
    private Double mapX;
    private Double mapY;
    private String tel;
    private String homepage;
    private int staffCnt;
    private int generalSiteCnt;
    private int carSiteCnt;
    private int glampingSiteCnt;
    private int caravanSiteCnt;
    private int personalCaravanSiteCnt;
    private int contentId;
    private String supportFacilities;
    private String outdoorActivities;
    private String petAccess;
    private String rentalGearList;
    private String operationDay;
    private String firstImageUrl;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private List<CampingFacilitiesDTO> campingFacilities;
    private CampingStatisticsDTO campingStatistics;  // CampingStatisticsDTO 필드 추가

    private int bookmarkCnt;
    private int reviewCnt;
}
