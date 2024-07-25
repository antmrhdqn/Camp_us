package com.commit.campus.dto;

import com.commit.campus.entity.CampingInfo;
import lombok.Getter;

@Getter
public class BookmarkRequest {

    private String userId;
    private String campId;
    private CampingInfo campInfo;
    private String createdBookmarkDate;

}
