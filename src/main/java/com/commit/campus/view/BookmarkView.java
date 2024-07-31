package com.commit.campus.view;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookmarkView {
    private Long userId;
    private Long campId;

}
