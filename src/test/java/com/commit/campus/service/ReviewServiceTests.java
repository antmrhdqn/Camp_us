package com.commit.campus.service;

import com.commit.campus.common.exceptions.ReviewAlreadyExistsException;
import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.entity.MyReview;
import com.commit.campus.entity.Review;
import com.commit.campus.repository.MyReviewRepository;
import com.commit.campus.repository.RatingSummaryRepository;
import com.commit.campus.repository.ReviewRepository;
import com.commit.campus.repository.UserRepository;
import com.commit.campus.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTests {

    @Mock private ReviewRepository reviewRepository;
    @Mock private MyReviewRepository myReviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private RatingSummaryRepository ratingSummaryRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        review = Review.builder()
                .reviewId(1L)  // Long 타입으로 지정
                .campId(1L)  // Long 타입으로 지정
                .userId(101L)  // Long 타입으로 지정
                .reviewContent("리뷰 테스트")
                .rating((byte) 5)
                .reviewCreatedDate(now)
                .reviewModificationDate(now)
                .reviewImageUrl("image1.jpg")
                .build();

        reviewDTO = new ReviewDTO();
        reviewDTO.setReviewId(1L);  // Long 타입으로 지정
        reviewDTO.setCampId(1L);  // Long 타입으로 지정
        reviewDTO.setUserId(101L);  // Long 타입으로 지정
        reviewDTO.setReviewContent("리뷰 테스트");
        reviewDTO.setRating((byte) 5);
        reviewDTO.setReviewImageUrl("image1.jpg");
    }


}
