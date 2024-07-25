package com.commit.campus.service.impl;

import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.entity.MyReview;
import com.commit.campus.entity.Review;
import com.commit.campus.repository.MyReviewRepository;
import com.commit.campus.repository.ReviewRepository;
import com.commit.campus.service.ReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MyReviewRepository myReviewRepository;
    private final ModelMapper modelMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, MyReviewRepository myReviewRepository, ModelMapper modelMapper) {
        this.reviewRepository = reviewRepository;
        this.myReviewRepository = myReviewRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<ReviewDTO> getReviewsByCampId(String campingSiteId, Pageable pageable) {
        return null;
    }

    @Override
    public void createReview(ReviewDTO reviewDTO) {

        Review review = modelMapper.map(reviewDTO, Review.class);
        Review savedReivew = reviewRepository.save(review);

        MyReview myReivew = MyReview.builder()
                .campId(savedReivew.getCampId())
                .userId(savedReivew.getUserId())
                .reviewId(savedReivew.getReviewId())
                .createdAt(LocalDateTime.now())
                .build();

        myReviewRepository.save(myReivew);
    }

    @Override
    public void updateReview(String reviewId, ReviewDTO reviewDTO) {
    }

    @Override
    public void deleteReview(String reviewId) {

    }
}
