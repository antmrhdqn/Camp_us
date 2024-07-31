package com.commit.campus.service;

import com.commit.campus.dto.SignUpUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class UserServiceTests {

    private UserService userService;

    @Autowired
    public UserServiceTests(UserService userService) {
        this.userService = userService;
    }

    @Test
    @Transactional
    void 회원가입_테스트() {
        //given
        SignUpUserRequest userRequest = SignUpUserRequest.builder()
                .email("testtest@naver.com")
                .password("test1234")
                .name("테스트")
                .nickname("별명")
                .build();

        //when
        userService.signUpUser(userRequest);

        //then
        Assertions.assertEquals(userRequest.getEmail(), userService.findUserByEmail("testtest@naver.com").getEmail());
    }
}
