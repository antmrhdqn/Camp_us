package com.commit.campus.service;

import com.commit.campus.dto.BookmarkedCampingDTO;
import com.commit.campus.dto.CampingDTO;
import com.commit.campus.entity.Camping;

import java.util.List;
import java.util.Optional;

public interface CampingService {

    List<Camping> getAllCampings();  // 모든 캠핑장 정보를 조회
    Camping createCamping(Camping camping);  // 새로운 캠핑장을 생성

    // 페이지네이션과 정렬을 적용하여 캠핑장 정보를 조회하는 메서드.
    List<Camping> getCampings(String doName, String sigunguName, Integer glampingSiteCnt, Integer caravanSiteCnt, int page, int size, String sort, String order);

    // 단일 캠핑장 정보를 조회하는 메서드.
    Optional<Camping> getCampingById(Long campId);

    // DTO 변환 메서드
    CampingDTO toCampingDTO(Camping camping);

    BookmarkedCampingDTO getBookmarkedCamping(Long campId);

}
