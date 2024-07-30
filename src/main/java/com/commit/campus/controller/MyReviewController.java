package com.commit.campus.controller;

import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.service.MyReviewService;
import com.commit.campus.service.ReviewService;
import com.commit.campus.view.MyReviewView;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/myReview")
public class MyReviewController {

    private final MyReviewService myReviewService;
    private final ReviewService reviewService;
    private final ModelMapper modelMapper;

    public MyReviewController(MyReviewService myReviewService, ReviewService reviewService, ModelMapper modelMapper) {
        this.myReviewService = myReviewService;
        this.reviewService = reviewService;
        this.modelMapper = modelMapper;
    }

    // 내 리뷰 정보 조회
    @GetMapping
    public ResponseEntity<Page<MyReviewView>> getMyReview(
            @PageableDefault(sort = "reviewCreatedDate") Pageable pageable) {

        long userId = 1; // TODO: 토큰에서 빼야 함

        Page<ReviewDTO> dtoPage = myReviewService.getUserReviews(userId, pageable);

        Page<MyReviewView> viewPage = dtoPage.map(reviewDTO -> modelMapper.map(reviewDTO, MyReviewView.class));
        return ResponseEntity.ok(viewPage);
    }

    // 내 리뷰 정보 삭제
}
