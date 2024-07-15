package com.commit.camp_reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String email;            // 유저 이메일
    private String passwd;           // 비밀번호
    private String name;             // 성명
    private String nickname;         // 닉네임
    private String birthDay;         // 생일
    private String registrationDate; // 가입일자
    private String withdrawDate;     // 탈퇴일자
    private String withdrawStatus;   // 탈퇴상태
    private String phoneNumber;      // 핸드폰번호
    private String userAddr;         // 주소
    private String profileImageUrl;  // 프로필 사진 URL
}
