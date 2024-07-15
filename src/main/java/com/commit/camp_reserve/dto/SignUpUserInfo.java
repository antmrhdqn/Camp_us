package com.commit.camp_reserve.dto;

import lombok.Getter;

@Getter
public class SignUpUserInfo {
    private int userId;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String birthDay;
    private String registrationDate;
    private String withdrawDate;
    private String withdrawStatus;
    private String phoneNumber;
    private String userAddr;
    private String profileImageUrl;
}
