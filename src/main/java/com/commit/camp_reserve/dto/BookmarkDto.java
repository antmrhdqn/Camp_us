package com.commit.camp_reserve.dto;

import lombok.*;

import java.util.Date;

@Getter
@Builder
public class BookmarkDto {
    private String userId;            // 유저
    private long campId;              // 캠핑장 식별키
    private int campInfo;             // 캠핑장 정보
    private Date createdBookmarkDate; // 즐겨찾기 날짜
}
