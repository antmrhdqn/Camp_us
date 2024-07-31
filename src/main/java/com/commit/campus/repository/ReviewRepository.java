package com.commit.campus.repository;

import com.commit.campus.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndCampId(long userId, long campId);

    Page<Review> findByReviewIdIn(List<Long> reviewIds, Pageable pageable);

}
