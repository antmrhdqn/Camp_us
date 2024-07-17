package com.commit.camp_reserve.dto;

import lombok.Getter;

@Getter
public class SignUpUserRequest {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String birthDay;
    private String phoneNumber;
    private String userAddr;
}
