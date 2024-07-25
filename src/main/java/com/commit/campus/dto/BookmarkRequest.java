package com.commit.campus.dto;

import lombok.Getter;

@Getter
public class BookmarkRequest {

    private String userId;
    private String campId;
    private CampingForDynamoDB campInfo;
    private String createdBookmarkDate;

}
