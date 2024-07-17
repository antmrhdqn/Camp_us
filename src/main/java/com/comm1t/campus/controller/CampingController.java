package com.comm1t.campus.controller;

import com.comm1t.campus.dto.CampingDTO;
import com.comm1t.campus.service.CampingService;
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
