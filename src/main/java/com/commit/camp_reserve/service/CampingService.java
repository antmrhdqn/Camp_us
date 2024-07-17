package com.commit.camp_reserve.service;

import com.commit.camp_reserve.dto.CampingDTO;

import java.util.List;

public interface CampingService {
    List<CampingDTO> getAllCampings();
    CampingDTO createCamping(CampingDTO campingDTO);
}
