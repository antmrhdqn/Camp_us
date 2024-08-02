package com.commit.campus.view;

import com.commit.campus.dto.CampingFacilitiesDTO;
import com.commit.campus.entity.Camping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Schema(description = "캠핑 뷰 모델")
public class CampingViewModel {
    @Schema(description = "캠핑장의 고유 ID")
    private Long campId;  // 캠핑장의 고유 ID
    @Schema(description = "캠핑장 이름")
    private String campName;  // 캠핑장 이름
    @Schema(description = "한줄 소개")
    private String lineIntro;  // 한줄 소개
    @Schema(description = "캠핑장 특징")
    private String featureSummary;  // 캠핑장 특징 요약
    @Schema(description = "주소")
    private String addr;  // 주소
    @Schema(description = "전화번호")
    private String tel;  // 전화번호
    @Schema(description = "홈페이지")
    private String homepage;  // 홈페이지 URL
    @Schema(description = "대표 이미지 URL")
    private String firstImageUrl;  // 대표 이미지 URL
    @Schema(description = "글램핑 사이트 수")
    private int glampingSiteCnt;  // 글램핑 사이트 수
    @Schema(description = "카라반 사이트 수")
    private int caravanSiteCnt;  // 카라반 사이트 수
    @Schema(description = "캠핑장 시설 목록")
    private List<CampingFacilitiesDTO> facilities;  // 캠핑장 시설 목록

    // Camping 엔티티 객체를 받아서 ViewModel 객체를 초기화하는 생성자
    public CampingViewModel(Camping entity) {
        this.campId = entity.getCampId();  // 캠핑장의 고유 ID 설정
        this.campName = entity.getCampName();  // 캠핑장 이름 설정
        this.lineIntro = entity.getLineIntro();  // 한줄 소개 설정
        this.featureSummary = entity.getFeatureSummary();  // 캠핑장 특징 요약 설정
        this.addr = entity.getAddr();  // 주소 설정
        this.tel = entity.getTel();  // 전화번호 설정
        this.homepage = entity.getHomepage();  // 홈페이지 URL 설정
        this.firstImageUrl = entity.getFirstImageUrl();  // 대표 이미지 URL 설정
        this.glampingSiteCnt = entity.getGlampingSiteCnt();  // 글램핑 사이트 수 설정
        this.caravanSiteCnt = entity.getCaravanSiteCnt();  // 카라반 사이트 수 설정
        // 캠핑장 시설 목록을 DTO로 변환하여 설정
        this.facilities = entity.getCampingFacilities().stream()
                .map(fac -> {
                    // CampingFacilitiesDTO 객체 생성 및 필드 설정
                    CampingFacilitiesDTO dto = new CampingFacilitiesDTO();
                    dto.setCampFacsId(fac.getCampFacsId());  // 캠핑 시설 고유 ID 설정
                    dto.setCampId(fac.getCampId());  // 캠핑장 ID 설정
                    dto.setFacsTypeId(fac.getFacsTypeId());  // 시설 유형 ID 설정
                    dto.setInternalFacilitiesList(fac.getInternalFacilitiesList());  // 내부 시설 목록 설정
                    dto.setToiletCnt(fac.getToiletCnt());  // 화장실 수 설정
                    dto.setShowerRoomCnt(fac.getShowerRoomCnt());  // 샤워실 수 설정
                    dto.setSinkCnt(fac.getSinkCnt());  // 개수대 수 설정
                    dto.setBrazierClass(fac.getBrazierClass());  // 화로대 종류 설정
                    dto.setPersonalTrailerStatus(fac.getPersonalTrailerStatus());  // 개인 트레일러 사용 가능 여부 설정
                    dto.setPersonalCaravanStatus(fac.getPersonalCaravanStatus());  // 개인 카라반 사용 가능 여부 설정
                    return dto;  // DTO 객체 반환
                })
                .collect(Collectors.toList());  // DTO 리스트로 수집하여 설정
    }
}
