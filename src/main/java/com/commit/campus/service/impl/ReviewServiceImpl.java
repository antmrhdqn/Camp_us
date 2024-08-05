package com.commit.campus.service.impl;

import com.commit.campus.common.exceptions.NotAuthorizedException;
import com.commit.campus.common.exceptions.ReviewAlreadyExistsException;
import com.commit.campus.common.exceptions.ReviewNotFoundException;
import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.entity.CampingSummary;
import com.commit.campus.entity.MyReview;
import com.commit.campus.entity.Review;
import com.commit.campus.repository.*;
import com.commit.campus.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MyReviewRepository myReviewRepository;
    private final UserRepository userRepository;
    private final RatingSummaryRepository ratingSummaryRepository;
    private final CampingSummaryRepository campingSummaryRepository;
    private final ModelMapper modelMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, MyReviewRepository myReviewRepository, UserRepository userRepository, RatingSummaryRepository ratingSummaryRepository, CampingSummaryRepository campingSummaryRepository, ModelMapper modelMapper) {
        this.reviewRepository = reviewRepository;
        this.myReviewRepository = myReviewRepository;
        this.userRepository = userRepository;
        this.ratingSummaryRepository = ratingSummaryRepository;
        this.campingSummaryRepository = campingSummaryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<ReviewDTO> getReviewsByCampId(long campId, Pageable pageable) {
        log.info("서비스 진입");
        Page<Review> reviewPage = reviewRepository.findByCampId(campId, pageable);
        log.info("반환 확인: {}", reviewPage);

        return reviewPage.map(review -> {
            ReviewDTO reviewDTO = modelMapper.map(review, ReviewDTO.class);
            log.info("DTO 확인: {}", reviewDTO);
            long userId = review.getUserId();
            log.info("id 확인: {}", userId);
            String userNickname = userRepository.findNicknameByUserId(userId);
            log.info("nickname 확인: {}", userNickname);
            reviewDTO.setUserNickname(userNickname);
            log.info("DTO 확인: {}", reviewDTO);
            return reviewDTO;
        });
    }

    @Override
    public void createReview(ReviewDTO reviewDTO) throws ReviewAlreadyExistsException {

        boolean reviewExists = reviewRepository.existsByUserIdAndCampId(reviewDTO.getUserId(), reviewDTO.getCampId());
        if (reviewExists) {
            throw new ReviewAlreadyExistsException("이미 이 캠핑장에 대한 리뷰를 작성하셨습니다.", HttpStatus.CONFLICT);
        }

        log.info("서비스 확인 reviewDTO {}", reviewDTO);
        Review review = Review.builder()
                .campId(reviewDTO.getCampId())
                .userId(reviewDTO.getUserId())
                .reviewContent(reviewDTO.getReviewContent())
                .rating(reviewDTO.getRating())
                .reviewCreatedDate(LocalDateTime.now())
                .reviewImageUrl(reviewDTO.getReviewImageUrl())
                .build();
        log.info("서비스 확인 entity {}", review);

        Review savedReview = reviewRepository.save(review);

        MyReview myReview = myReviewRepository.findById(savedReview.getUserId())
                .orElse(new MyReview(savedReview.getUserId()));

        myReview.incrementReviewCnt(savedReview.getReviewId());
        log.info("서비스 확인 myreview {}", myReview);
        myReviewRepository.save(myReview);
        log.info("서비스 확인 내 리뷰 저장 완료");

        ratingSummaryRepository.incrementRating(savedReview.getCampId(), savedReview.getRating());

        CampingSummary campingSummary = campingSummaryRepository.findById(savedReview.getCampId())
                .orElseGet(() -> CampingSummary.builder()
                        .campId(savedReview.getCampId())
                        .bookmarkCnt(0)
                        .reviewCnt(0)
                        .build());

        campingSummary.incrementReviewCnt();
        campingSummaryRepository.save(campingSummary);
    }

    @Override
    public void updateReview(ReviewDTO reviewDTO, long userId) {
        Review originReview = reviewRepository.findById(reviewDTO.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("엔티티를 찾을 수 없습니다."));
        log.info("서비스 확인: {}", originReview);

        if (!(originReview.getUserId() == userId)) {
            throw new NotAuthorizedException("이 리뷰를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        Review updatedReview = updateReviewFromDTO(originReview, reviewDTO);
        log.info("서비스 확인 {}", updatedReview);

        reviewRepository.save(updatedReview);

        ratingSummaryRepository.decrementRating(originReview.getCampId(), originReview.getRating());
        ratingSummaryRepository.incrementRating(updatedReview.getCampId(), updatedReview.getRating());

    }

    @Override
    public void deleteReview(long reviewId, long userId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("작성된 리뷰가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        long reviewer = review.getUserId();

        if (!(reviewer == userId)) {
            throw new NotAuthorizedException("이 리뷰를 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        CampingSummary campingSummary = campingSummaryRepository.findById(review.getCampId())
                .orElseThrow(() -> new IllegalStateException("해당 캠핑장의 리뷰 정보가 존재하지 않습니다. 데이터 무결성 문제가 있을 수 있습니다."));

        campingSummary.decrementReviewCnt();
        campingSummaryRepository.save(campingSummary);

        ratingSummaryRepository.decrementRating(review.getCampId(), review.getRating());

        MyReview myReview = myReviewRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자의 리뷰 정보가 존재하지 않습니다. 데이터 무결성 문제가 있을 수 있습니다."));

        myReview.decrementReviewCnt(reviewId);
        myReviewRepository.save(myReview);

        reviewRepository.delete(review);
    }

    private Review updateReviewFromDTO(Review review, ReviewDTO reviewDTO) {
        return Review.builder()
                .reviewId(review.getReviewId())
                .campId(review.getCampId())
                .userId(review.getUserId())
                .reviewContent(reviewDTO.getReviewContent() != null ? reviewDTO.getReviewContent() : review.getReviewContent())
                .rating(reviewDTO.getRating())
                .reviewCreatedDate(review.getReviewCreatedDate())
                .reviewModificationDate(LocalDateTime.now())
                .reviewImageUrl(reviewDTO.getReviewImageUrl() != null ? reviewDTO.getReviewImageUrl() : review.getReviewImageUrl())
                .build();
    }
}
