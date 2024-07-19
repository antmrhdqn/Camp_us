package com.commit.campus.service;

import com.commit.campus.dto.CampingDTO;

import java.util.List;

public interface CampingService {
    List<CampingDTO> getAllCampings();
    CampingDTO createCamping(CampingDTO campingDTO);
}
