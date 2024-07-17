package com.commit.camp_reserve.controller;

import com.commit.camp_reserve.dto.CampingDTO;
import com.commit.camp_reserve.service.CampingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/campings")
public class CampingController {

    @Autowired
    private CampingService campingService;

    @GetMapping
    public List<CampingDTO> getAllCampings() {
        return campingService.getAllCampings();
    }

    @PostMapping
    public CampingDTO createCamping(@RequestBody CampingDTO campingDTO) {
        return campingService.createCamping(campingDTO);
    }
}
