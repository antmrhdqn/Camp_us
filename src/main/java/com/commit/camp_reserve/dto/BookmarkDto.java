package com.commit.camp_reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkDto {
    private String userId;           // 유저 이메일
    private int campId;              // 캠핑장 식별키
    private int campInfo;            // 캠핑장 정보
    private Date createdBookmarkDate;// 즐겨찾기 날짜
}
