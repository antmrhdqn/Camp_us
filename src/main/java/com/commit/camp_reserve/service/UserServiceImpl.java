package com.commit.camp_reserve.service;

import com.commit.camp_reserve.dto.SignUpUserRequest;
import com.commit.camp_reserve.entity.User;
import com.commit.camp_reserve.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public void signUpUser(SignUpUserRequest userRequest) {

        LocalDateTime currentTime = LocalDateTime.now();
        User user = User.builder()
                .email(userRequest.getEmail())
                .name(userRequest.getName())
                .password(userRequest.getPassword())
                .nickname(userRequest.getNickname())
                .birthDay(userRequest.getBirthDay())
                .registrationDate(currentTime)
                .enrollDate(currentTime)
                .phoneNumber(userRequest.getPhoneNumber())
                .userAddr(userRequest.getUserAddr())
                .profileImageUrl(userRequest.getProfileImageUrl())
                .build();
        userRepository.save(user);
    }
}
