package com.commit.campus.service.impl;

import com.commit.campus.dto.BookmarkedCampingDTO;
import com.commit.campus.dto.CampingDTO;
import com.commit.campus.entity.Camping;
import com.commit.campus.repository.CampingRepository;
import com.commit.campus.service.CampingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CampingServiceImpl implements CampingService {

    private CampingRepository campingRepository;

    @Autowired
    public CampingServiceImpl(CampingRepository campingRepository) {
        this.campingRepository = campingRepository;
    }


    @Override
    public List<CampingDTO> getAllCampings() {
        return null;
    }

    @Override
    public CampingDTO createCamping(CampingDTO campingDTO) {
        return null;
    }

    @Override
    public BookmarkedCampingDTO getBookmarkedCamping(Long campId) {
        Camping camping = campingRepository.findById(campId).orElseThrow(IllegalArgumentException::new);

        String induty = Optional.ofNullable(camping.getInduty()).orElse("");
        String firstImageUrl = Optional.ofNullable(camping.getFirstImageUrl()).orElse("");

        BookmarkedCampingDTO bookmarkedCampingDTO = BookmarkedCampingDTO.builder()
                .campName(camping.getCampName())
                .doName(camping.getDoName())
                .sigunguName(camping.getSigunguName())
                .postCode(camping.getPostCode())
                .induty(camping.getInduty())
                .firstImageUrl(camping.getFirstImageUrl())
                .build();

        return bookmarkedCampingDTO;
    }
}

