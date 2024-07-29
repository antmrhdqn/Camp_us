package com.commit.campus.service;

import com.commit.campus.dto.BookmarkDTO;
import com.commit.campus.dto.BookmarkRequest;

import java.util.List;

public interface BookmarkService {

    void saveBookmark(BookmarkRequest bookmarkRequest);

    List<BookmarkDTO> getBookmark(Long userId);
}
