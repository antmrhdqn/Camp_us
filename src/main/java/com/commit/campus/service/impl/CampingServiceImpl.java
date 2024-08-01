package com.commit.campus.service.impl;

import com.commit.campus.dto.BookmarkedCampingDTO;
import com.commit.campus.dto.CampingDTO;
import com.commit.campus.dto.CampingFacilitiesDTO;
import com.commit.campus.entity.Camping;
import com.commit.campus.entity.CampingFacilities;
import com.commit.campus.entity.CampingSummary;
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
        return campingRepository.findById(campId);
    }

    @Override
    public CampingDTO toCampingDTO(Camping camping) {
        CampingDTO dto = new CampingDTO();
        BeanUtils.copyProperties(camping, dto);
        List<CampingFacilitiesDTO> facilitiesDTOList = camping.getCampingFacilities().stream()
                .map(this::convertToFacilitiesDTO)
                .collect(Collectors.toList());
        dto.setCampingFacilities(facilitiesDTOList);

        // CampingSummary 엔티티에서 값을 가져와서 DTO에 설정
        CampingSummary campingSummary = camping.getCampingSummary();
        if (campingSummary != null) {
            dto.setBookmarkCnt(campingSummary.getBookmarkCnt());
            dto.setReviewCnt(campingSummary.getReviewCnt());
        } else {
            dto.setBookmarkCnt(0);
            dto.setReviewCnt(0);
        }

        return dto;
    }

    @Override
    public List<CampingDTO> getAllCampingsSortedByBookmarks() {
        List<Camping> campings = campingRepository.findAllOrderByBookmarkCntDesc();
        return campings.stream()
                .map(this::toCampingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getAllCampingsSortedByReviews() {
        List<Camping> campings = campingRepository.findAllOrderByReviewCntDesc();
        return campings.stream()
                .map(this::toCampingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookmarkedCampingDTO getBookmarkedCamping(Long campId) {
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
