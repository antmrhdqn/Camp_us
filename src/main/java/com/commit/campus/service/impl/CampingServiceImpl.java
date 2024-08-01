package com.commit.campus.service.impl;

import com.commit.campus.dto.CampingDTO;
import com.commit.campus.dto.CampingFacilitiesDTO;
import com.commit.campus.entity.Camping;
import com.commit.campus.entity.CampingFacilities;
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
        return dto;
    }

    @Override
    public List<Camping> getAllCampingsSortedByBookmarks() {
        return campingRepository.findAllOrderByBookmarkCntDesc();
    }

    @Override
    public List<Camping> getAllCampingsSortedByReviews() {
        return campingRepository.findAllOrderByReviewCntDesc();
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
