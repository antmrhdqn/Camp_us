package com.commit.camp_reserve.controller;

import com.commit.camp_reserve.dto.CampingDto;
import com.commit.camp_reserve.service.CampingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/campings")
public class CampingController {

    private final CampingService campingService;

    @Autowired
    public CampingController(CampingService campingService) {
        this.campingService = campingService;
    }

    @GetMapping("/basedList")
    public List<CampingDto> getBasedList() {
        return campingService.getBasedList();
    }
}
