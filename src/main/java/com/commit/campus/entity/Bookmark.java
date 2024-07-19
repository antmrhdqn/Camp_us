package com.commit.campus.entity;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;  // 즐겨찾기 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // 사용자 ID

    @ManyToOne
    @JoinColumn(name = "camp_id")
    private Camping camping;  // 캠핑장 ID

    private String campInfo;  // 캠핑장 정보
    private Date createdBookmarkDate;  // 즐겨찾기 생성 날짜
}
