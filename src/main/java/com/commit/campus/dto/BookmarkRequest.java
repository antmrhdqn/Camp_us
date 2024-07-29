package com.commit.campus.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookmarkRequest {

    private Long userId;
    private Long campId;

}
