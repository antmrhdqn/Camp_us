package com.commit.campus.service.impl;

import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.entity.Review;
import com.commit.campus.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Override
    public Page<ReviewDTO> getReviewsByCampId(String campingSiteId, Pageable pageable) {
        return null;
    }

    @Override
    public Review createReview(ReviewDTO reviewDTO) {
        return null;
    }

    @Override
    public Review updateReview(String reviewId, ReviewDTO reviewDTO) {
        return null;
    }

    @Override
    public void deleteReview(String reviewId) {

    }
}
