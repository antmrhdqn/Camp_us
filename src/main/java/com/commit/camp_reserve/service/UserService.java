package com.commit.camp_reserve.service;


import com.commit.camp_reserve.dto.SignUpUserRequest;
import com.commit.camp_reserve.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User findUserByEmail(String email);
    void signUpUser(SignUpUserRequest userRequest);
}
