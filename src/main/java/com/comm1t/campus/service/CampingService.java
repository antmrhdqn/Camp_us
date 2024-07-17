package com.comm1t.campus.service;

import com.comm1t.campus.dto.CampingDTO;

import java.util.List;

public interface CampingService {
    List<CampingDTO> getAllCampings();
    CampingDTO createCamping(CampingDTO campingDTO);
}
