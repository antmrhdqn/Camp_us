package com.commit.camp_reserve.controller;

import com.commit.camp_reserve.dto.SignUpUserInfo;
import com.commit.camp_reserve.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("sign-up")
    public ResponseEntity<Void> signUpUser(@RequestBody SignUpUserInfo userInfo) {
        userService.signUpUser(userInfo);
        return ResponseEntity.ok().build();
    }

}
