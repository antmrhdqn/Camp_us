package com.commit.camp_reserve.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="user")
@Getter
@Setter
@ToString
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String nickname;

    @Column
    private String birthDay;

    @Column
    private String registrationDate;

    @Column
    private String withdrawDate;

    @Column
    private String withdrawStatus;

    @Column
    private String phoneNumber;

    @Column
    private String userAddr;

    @Column
    private String profileImageUrl;
}
