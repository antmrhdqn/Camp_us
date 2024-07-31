package com.commit.campus.service.impl;

import com.commit.campus.common.exceptions.ReviewNotFoundException;
import com.commit.campus.dto.MyReviewDTO;
import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.entity.MyReview;
import com.commit.campus.entity.Review;
import com.commit.campus.repository.MyReviewRepository;
import com.commit.campus.repository.ReviewRepository;
import com.commit.campus.service.MyReviewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class MyReviewServiceImpl implements MyReviewService {

    private final MyReviewRepository myReviewRepository;
    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;

    public MyReviewServiceImpl(MyReviewRepository myReviewRepository, ReviewRepository reviewRepository, ModelMapper modelMapper) {
        this.myReviewRepository = myReviewRepository;
        this.reviewRepository = reviewRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Page<ReviewDTO> getMyReviews(long userId, Pageable pageable) throws ReviewNotFoundException {

        MyReview myReview = myReviewRepository.findById(userId)
                .orElseThrow(() -> new ReviewNotFoundException("작성된 리뷰가 존재하지 않습니다."));

        List<Long> reviewIds = myReview.getReviewIds();
        log.info("reviewIds type: " + reviewIds.getClass().getName());
        log.info("reviewIds content: " + reviewIds);

        log.info("진행 확인");
        Page<Review> reviewPage = reviewRepository.findByReviewIdIn(reviewIds, pageable);

        log.info("진행 확인2");
        Page<ReviewDTO> dtoPage = reviewPage.map(review -> modelMapper.map(review, ReviewDTO.class));

        return dtoPage;
    }
}
