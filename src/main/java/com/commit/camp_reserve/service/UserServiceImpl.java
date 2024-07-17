package com.commit.camp_reserve.service;

import com.commit.camp_reserve.dto.SignUpUserRequest;
import com.commit.camp_reserve.entity.User;
import com.commit.camp_reserve.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    @Override
    public void signUpUser(SignUpUserRequest userRequest) {

        LocalDateTime currentTime = LocalDateTime.now();
        User user = User.builder()
                .email(userRequest.getEmail())
                .name(userRequest.getName())
                .password(bCryptPasswordEncoder.encode(userRequest.getPassword()))
                .nickname(userRequest.getNickname())
                .birthDay(userRequest.getBirthDay())
                .registrationDate(currentTime)
                .enrollDate(currentTime)
                .phoneNumber(userRequest.getPhoneNumber())
                .userAddr(userRequest.getUserAddr())
                .role("ROLE_USER")
                .build();
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다: " + email);
        }
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findUserByEmail(email);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        return authorities;
    }
}
