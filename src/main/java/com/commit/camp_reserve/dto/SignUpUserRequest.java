package com.commit.camp_reserve.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class SignUpUserRequest {
    private Long userId;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String birthDay;
    private LocalDateTime registrationDate;
    private LocalDateTime enrollDate;
    private String phoneNumber;
    private String userAddr;
    private String profileImageUrl;
}
