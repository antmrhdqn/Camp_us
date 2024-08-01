package com.commit.campus.service.impl;

import com.commit.campus.dto.BookmarkedCampingDTO;  // 추가된 부분
import com.commit.campus.dto.CampingDTO;
import com.commit.campus.dto.CampingFacilitiesDTO;
import com.commit.campus.dto.CampingStatisticsDTO;  // 추가된 부분
import com.commit.campus.entity.Camping;
import com.commit.campus.entity.CampingFacilities;
import com.commit.campus.entity.CampingStatistics;  // 추가된 부분
import com.commit.campus.repository.CampingRepository;
import com.commit.campus.service.CampingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CampingServiceImpl implements CampingService {

    @Autowired
    private CampingRepository campingRepository;

    @Override
    public List<Camping> getAllCampings() {
        return campingRepository.findAll();
    }

    @Override
    public Camping createCamping(Camping camping) {
        return campingRepository.save(camping);
    }

    @Override
    public List<Camping> getCampings(String doName, String sigunguName, Integer glampingSiteCnt, Integer caravanSiteCnt, int page, int size, String sort, String order) {
        int offset = page * size;
        List<Camping> campings = campingRepository.findCampings(doName, sigunguName, glampingSiteCnt, caravanSiteCnt, offset, size);
        return campings.stream()
                .sorted(getComparator(sort, order))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Camping> getCampingById(Long campId) {
        return campingRepository.findById(campId);  // 기존 단일 조회 메서드 유지
    }

    @Override
    public List<CampingDTO> getAllCampingsSortedByBookmarks() {  // 찜한 수로 정렬된 캠핑장 리스트를 조회하는 메서드
        List<Camping> campings = campingRepository.findAllOrderByBookmarkCntDesc();  // 찜한 수로 내림차순 정렬된 캠핑장 리스트 조회
        return campings.stream()
                .map(this::toCampingDTO)  // 캠핑 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getAllCampingsSortedByReviews() {  // 리뷰 수로 정렬된 캠핑장 리스트를 조회하는 메서드
        List<Camping> campings = campingRepository.findAllOrderByReviewCntDesc();  // 리뷰 수로 내림차순 정렬된 캠핑장 리스트 조회
        return campings.stream()
                .map(this::toCampingDTO)  // 캠핑 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Override
    public CampingDTO toCampingDTO(Camping camping) {
        CampingDTO dto = new CampingDTO();
        BeanUtils.copyProperties(camping, dto);
        List<CampingFacilitiesDTO> facilitiesDTOList = camping.getCampingFacilities().stream()
                .map(this::convertToFacilitiesDTO)
                .collect(Collectors.toList());
        dto.setCampingFacilities(facilitiesDTOList);

        // CampingStatistics 엔티티에서 값을 가져와서 DTO에 설정
        CampingStatistics campingStatistics = camping.getCampingStatistics();  // 캠핑장의 통계 정보를 가져옴
        if (campingStatistics != null) {
            CampingStatisticsDTO campingStatisticsDTO = new CampingStatisticsDTO(
                    campingStatistics.getCampId(),
                    campingStatistics.getBookmarkCnt(),
                    campingStatistics.getReviewCnt()
            );
            dto.setCampingStatistics(campingStatisticsDTO);  // DTO에 통계 정보를 설정
        }

        return dto;
    }

    @Override
    public BookmarkedCampingDTO getBookmarkedCamping(Long campId) {  // 추가된 메서드
        Camping camping = campingRepository.findById(campId).orElseThrow(() -> new IllegalArgumentException("Invalid campId: " + campId));
        return BookmarkedCampingDTO.builder()
                .campName(camping.getCampName())
                .doName(camping.getDoName())
                .sigunguName(camping.getSigunguName())
                .postCode(camping.getPostCode())
                .induty(camping.getInduty())
                .firstImageUrl(camping.getFirstImageUrl())
                .build();
    }

    private CampingFacilitiesDTO convertToFacilitiesDTO(CampingFacilities facilities) {
        CampingFacilitiesDTO dto = new CampingFacilitiesDTO();
        BeanUtils.copyProperties(facilities, dto);
        return dto;
    }

    private Comparator<Camping> getComparator(String sort, String order) {
        Comparator<Camping> comparator;

        switch (sort) {
            case "campName":
                comparator = Comparator.comparing(Camping::getCampName, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "createdDate":
                comparator = Comparator.comparing(Camping::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            default:
                comparator = Comparator.comparing(Camping::getCampId, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }
}
