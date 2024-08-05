package com.commit.campus.service.impl;

import com.commit.campus.common.exceptions.NotAuthorizedException;
import com.commit.campus.common.exceptions.ReviewAlreadyExistsException;
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
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getReviewsByCampId(long campId, Pageable pageable) {

        Page<Review> reviewPage = reviewRepository.findByCampId(campId, pageable);

        return reviewPage.map(this::mapToReviewWithNickname);
    }

    @Override
    public void createReview(ReviewDTO reviewDTO) throws ReviewAlreadyExistsException {

        checkExistingReview(reviewDTO.getUserId(), reviewDTO.getCampId());

        Review savedReview = saveReview(reviewDTO);
        updateMyReview(savedReview);
        updateRatingSummary(savedReview);
        updateCampingSummary(savedReview);
    }

    @Override
    public void updateReview(ReviewDTO reviewDTO, long userId) {

        Review originReview = findReviewById(reviewDTO.getReviewId());

        verifyReviewEditPermission(originReview.getUserId(), userId);

        Review updatedReview = updateReviewFromDTO(originReview, reviewDTO);
        reviewRepository.save(updatedReview);

        updateRatingSummary(originReview, updatedReview);
    }

    @Override
    public void deleteReview(long reviewId, long userId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("작성된 리뷰가 존재하지 않습니다."));

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

    private ReviewDTO mapToReviewWithNickname(Review review) {
        ReviewDTO reviewDTO = modelMapper.map(review, ReviewDTO.class);
        String userNickname = userRepository.findNicknameByUserId(review.getUserId());
        reviewDTO.setUserNickname(userNickname);
        return reviewDTO;
    }

    private void checkExistingReview(long userId, long campId) {
        if (reviewRepository.existsByUserIdAndCampId(userId, campId)) {
            throw new ReviewAlreadyExistsException("이미 이 캠핑장에 대한 리뷰를 작성하셨습니다.", HttpStatus.CONFLICT);
        }
    }

    private Review saveReview(ReviewDTO reviewDTO) {
        Review review = Review.builder()
                .campId(reviewDTO.getCampId())
                .userId(reviewDTO.getUserId())
                .reviewContent(reviewDTO.getReviewContent())
                .rating(reviewDTO.getRating())
                .reviewCreatedDate(LocalDateTime.now())
                .reviewImageUrl(reviewDTO.getReviewImageUrl())
                .build();
        return reviewRepository.save(review);
    }

    private void updateMyReview(Review savedReview) {
        MyReview myReview = myReviewRepository.findById(savedReview.getUserId())
                .orElse(new MyReview(savedReview.getUserId()));
        myReview.incrementReviewCnt(savedReview.getReviewId());
        myReviewRepository.save(myReview);
    }

    private void updateRatingSummary(Review savedReview) {
        ratingSummaryRepository.incrementRating(savedReview.getCampId(), savedReview.getRating());
    }

    private void updateCampingSummary(Review savedReview) {
        CampingSummary campingSummary = campingSummaryRepository.findById(savedReview.getCampId())
                .orElseGet(() -> CampingSummary.builder()
                        .campId(savedReview.getCampId())
                        .bookmarkCnt(0)
                        .reviewCnt(0)
                        .build());
        campingSummary.incrementReviewCnt();
        campingSummaryRepository.save(campingSummary);
    }

    private Review findReviewById(long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
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

    private void verifyReviewEditPermission(long reviewerId, long userId) {
        if (reviewerId != userId) {
            throw new NotAuthorizedException("이 리뷰를 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void updateRatingSummary(Review oldReview, Review newReview) {
        ratingSummaryRepository.decrementRating(oldReview.getCampId(), oldReview.getRating());
        ratingSummaryRepository.incrementRating(newReview.getCampId(), newReview.getRating());
    }
}
