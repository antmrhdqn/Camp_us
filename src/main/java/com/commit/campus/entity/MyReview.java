package com.commit.campus.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "my_riview")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyReview {

    @Id
    @GeneratedValue
    @Column(name = "my_review_id")
    private Long myReviewId;

    @Column(name = "camp_id")
    private Long campId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
