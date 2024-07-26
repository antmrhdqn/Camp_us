package com.commit.campus.controller;

import com.commit.campus.common.view.ReviewView;
import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.dto.ReviewRequest;
import com.commit.campus.entity.Review;
import com.commit.campus.service.ReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ModelMapper modelMapper;

    public ReviewController(ReviewService reviewService, ModelMapper modelMapper) {
        this.reviewService = reviewService;
        this.modelMapper = modelMapper;
    }

    // 캠핑장 리뷰 조회
    @GetMapping("/camping/{campId}")
    public ResponseEntity<Page<ReviewView>> getReviewsByCampId(
            @PathVariable String campId,
            @PageableDefault(sort = "reviewCreatedDate", direction = Sort.Direction.DESC)Pageable pageable) {

        Page<ReviewDTO> dtoPage = reviewService.getReviewsByCampId(campId, pageable);
        Page<ReviewView> viewPage = dtoPage.map(reviewDTO -> modelMapper.map(reviewDTO, ReviewView.class));

        return ResponseEntity.ok(viewPage);
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<Void> createReview(@RequestBody ReviewRequest reviewRequest) {

        ReviewDTO reviewDTO = modelMapper.map(reviewRequest, ReviewDTO.class);
        reviewService.createReview(reviewDTO);

        return ResponseEntity.noContent().build();
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(@PathVariable String reviewId, @RequestBody ReviewRequest reviewRequest) {

        ReviewDTO reviewDTO = modelMapper.map(reviewRequest, ReviewDTO.class);
        reviewService.updateReview(reviewId, reviewDTO);

        return ResponseEntity.noContent().build();
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")

    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {

        reviewService.deleteReview(reviewId);

        return ResponseEntity.noContent().build();
    }
}