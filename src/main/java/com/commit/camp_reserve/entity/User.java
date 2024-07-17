package com.commit.camp_reserve.entity;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;  // key

    private String email;  // 이메일
    private String password;  // 비밀번호
    private String name;  // 이름
    private String nickname;  // 닉네임
    private Date birthDay;  // 생일
    private String phoneNumber;  // 전화번호
    private String userAddr;  // 주소
    private String profileImageUrl;  // 프로필 이미지 URL
    private Date enrollDate;  // 등록 날짜
    private Date registrationDate;  // 가입 날짜
    private String role;  // 역할
}