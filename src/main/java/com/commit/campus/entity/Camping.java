package com.commit.campus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "camping")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Camping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "camp_id")
    private Long campId;

    @Column(name = "camp_name")
    private String campName;

    @Column(name = "line_intro")
    private String lineIntro;

    @Column(name = "intro", columnDefinition = "TEXT")
    private String intro;

    @Column(name = "do_name")
    private String doName;

    @Column(name = "sigungu_name")
    private String sigunguName;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "feature_summary", columnDefinition = "TEXT")
    private String featureSummary;

    @Column(name = "induty")
    private String induty;

    @Column(name = "addr")
    private String addr;

    @Column(name = "addr_details")
    private String addrDetails;

    @Column(name = "mapX")
    private Double mapX;

    @Column(name = "mapY")
    private Double mapY;

    @Column(name = "tel")
    private String tel;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "staff_cnt")
    private int staffCnt;

    @Column(name = "general_site_cnt")
    private int generalSiteCnt;

    @Column(name = "car_site_cnt")
    private int carSiteCnt;

    @Column(name = "glamping_site_cnt")
    private int glampingSiteCnt;

    @Column(name = "caravan_site_cnt")
    private int caravanSiteCnt;

    @Column(name = "personal_caravan_site_cnt")
    private int personalCaravanSiteCnt;

    @Column(name = "support_facilities")
    private String supportFacilities; // 부대시설(편의시설)

    @Column(name = "outdoor_activities")
    private String outdoorActivities; // 주변시설

    @Column(name = "pet_access")
    private String petAccess; // 반려동물 출입 여부

    @Column(name = "rental_gear_list")
    private String rentalGearList; // 대여 장비 목록

    @Column(name = "operation_day")
    private String operationDay; // 운영일

    @Column(name = "first_image_url")
    private String firstImageUrl; // 대표이미지

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }

}
