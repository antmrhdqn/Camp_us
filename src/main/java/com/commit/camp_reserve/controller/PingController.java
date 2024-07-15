package com.commit.camp_reserve.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ping")
public class PingController {

    // 연동 확인 test
    @GetMapping
    public String ping() {
        return "Hello";
    }
}
