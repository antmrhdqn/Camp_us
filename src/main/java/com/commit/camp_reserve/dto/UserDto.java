package com.commit.camp_reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private long userId;           // key
    private String email;            // 유저 이메일
    private String passwd;           // 비밀번호
    private String name;             // 성명
    private String nickname;         // 닉네임
    private String birthDay;         // 생일
    private String phoneNumber;      // 핸드폰번호
    private String userAddr;         // 주소
    private String profileImageUrl;  // 프로필 사진 URL
    private LocalDateTime enrollDate;       // 가입일시
    private LocalDateTime registrationDate; // 등록일시
}
