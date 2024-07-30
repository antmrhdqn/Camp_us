package com.commit.campus.service;

import com.commit.campus.common.exceptions.ReviewAlreadyExistsException;
import com.commit.campus.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    Page<ReviewDTO> getReviewsByCampId(String campingSiteId, Pageable pageable);

    void createReview(ReviewDTO reviewDTO) throws ReviewAlreadyExistsException;

    void updateReview(String reviewId, ReviewDTO reviewDTO);

    void deleteReview(String reviewId);
}
