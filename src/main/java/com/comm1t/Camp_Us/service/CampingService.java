package com.comm1t.Camp_Us.service;

import com.comm1t.Camp_Us.dto.CampingDTO;

import java.util.List;

public interface CampingService {
    List<CampingDTO> getAllCampings();
    CampingDTO createCamping(CampingDTO campingDTO);
}
