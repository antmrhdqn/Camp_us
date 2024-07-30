package com.commit.campus.service;

import com.commit.campus.common.exceptions.ReviewNotFoundException;
import com.commit.campus.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MyReviewService {
    Page<ReviewDTO> getUserReviews(long userId, Pageable pageable) throws ReviewNotFoundException;
}
