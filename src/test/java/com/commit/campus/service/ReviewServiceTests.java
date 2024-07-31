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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
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
                .reviewId(1L)
                .campId(1L)
                .userId(101L)
                .reviewContent("리뷰 테스트")
                .rating((byte) 5)
                .reviewCreatedDate(now)
                .reviewModificationDate(now)
                .reviewImageUrl("image1.jpg")
                .build();

        reviewDTO = new ReviewDTO();
        reviewDTO.setReviewId(1L);
        reviewDTO.setCampId(1L);
        reviewDTO.setUserId(101L);
        reviewDTO.setReviewContent("리뷰 테스트");
        reviewDTO.setRating((byte) 5);
        reviewDTO.setReviewImageUrl("image1.jpg");
    }

    @Test
    void 캠핑장_리뷰_조회() {
        // Given
        var pageable = mock(Pageable.class);
        var reviewPage = new PageImpl<>(Arrays.asList(review));

        when(reviewRepository.findByCampId(1L, pageable)).thenReturn(reviewPage);
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);
        when(userRepository.findNicknameByUserId(anyLong())).thenReturn("nickname");

        // When
        Page<ReviewDTO> result = reviewService.getReviewsByCampId(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0))
                .extracting("reviewId", "campId", "userId", "reviewContent", "rating", "reviewImageUrl", "userNickname")
                .containsExactly(1L, 1L, 101L, "리뷰 테스트", (byte) 5, "image1.jpg", "nickname");

        verify(reviewRepository).findByCampId(1L, pageable);
        verify(userRepository).findNicknameByUserId(1L);
    }

    @Test
    void 캠핑장_정상_등록() {
        // Given
        var pageable = PageRequest.of(0, 10);
        var reviewPage = new PageImpl<Review>(List.of(), pageable, 0);

        when(reviewRepository.findByCampId(1L, pageable)).thenReturn(reviewPage);

        // When
        Page<ReviewDTO> result = reviewService.getReviewsByCampId(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 캠핑장_리뷰_이미_존재_예외() {
        // Given
        when(reviewRepository.existsByUserIdAndCampId(reviewDTO.getUserId(), reviewDTO.getCampId())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(reviewDTO))
                .isInstanceOf(ReviewAlreadyExistsException.class)
                .hasMessageContaining("이미 이 캠핑장에 대한 리뷰를 작성하셨습니다.");

        verify(reviewRepository, never()).save(any(Review.class));
        verify(myReviewRepository, never()).save(any(MyReview.class));
        verify(ratingSummaryRepository, never()).ratingUpdate(anyLong(), anyByte());
    }
}
