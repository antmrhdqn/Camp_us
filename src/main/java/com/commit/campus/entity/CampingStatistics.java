package com.commit.campus.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "camping_statistics")
@Getter
@Builder
@NoArgsConstructor
@DynamicUpdate
@ToString
public class CampingStatistics {

    @Id
    @Column(name = "camp_id")
    private Long campId;

    @Column(name = "bookmark_cnt", nullable = false)
    private int bookmarkCnt;

    @Column(name = "review_cnt", nullable = false)
    private int reviewCnt;

    public CampingStatistics(Long campId, int bookmarkCnt, int reviewCnt) {
        this.campId = campId;
        this.bookmarkCnt = bookmarkCnt;
        this.reviewCnt = reviewCnt;
    }

    public void setBookmarkCnt(int bookmarkCnt) {
        this.bookmarkCnt = bookmarkCnt;
    }

    public void setReviewCnt(int reviewCnt) {
        this.reviewCnt = reviewCnt;
    }
}
