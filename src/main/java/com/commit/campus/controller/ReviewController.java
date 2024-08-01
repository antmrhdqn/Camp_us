package com.commit.campus.controller;

import com.commit.campus.view.ReviewView;
import com.commit.campus.dto.ReviewDTO;
import com.commit.campus.dto.ReviewRequest;
import com.commit.campus.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/reviews")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final ModelMapper modelMapper;

    public ReviewController(ReviewService reviewService, ModelMapper modelMapper) {
        this.reviewService = reviewService;
        this.modelMapper = modelMapper;
    }

    // 캠핑장 리뷰 조회
    @GetMapping()
    public ResponseEntity<Page<ReviewView>> getReviewsByCampId(
            @RequestParam("campId") long campId,
            @PageableDefault(sort = "reviewCreatedDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewDTO> dtoPage = reviewService.getReviewsByCampId(campId, pageable);
        Page<ReviewView> viewPage = dtoPage.map(reviewDTO -> modelMapper.map(reviewDTO, ReviewView.class));

        return ResponseEntity.ok(viewPage);
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<Void> createReview(@RequestBody ReviewRequest reviewRequest) {

        long userId = 1; // TODO: 토큰에서 빼내야함

        log.info("컨트롤러 확인 request {}", reviewRequest);
        ReviewDTO reviewDTO = modelMapper.map(reviewRequest, ReviewDTO.class);
        reviewDTO.setUserId(userId);
        log.info("컨트롤러 확인 DTO {}", reviewDTO);
        reviewService.createReview(reviewDTO);

        return ResponseEntity.noContent().build();
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(@PathVariable long reviewId, @RequestBody ReviewRequest reviewRequest) {

        ReviewDTO reviewDTO = modelMapper.map(reviewRequest, ReviewDTO.class);
        reviewService.updateReview(reviewId, reviewDTO);

        return ResponseEntity.noContent().build();
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")

    public ResponseEntity<Void> deleteReview(@PathVariable long reviewId) {

        long userId = 1; // TODO: 토큰에서 빼내야 함
        reviewService.deleteReview(reviewId, userId);

        return ResponseEntity.noContent().build();
    }
}