package com.commit.campus.service.impl;

import com.commit.campus.dto.BookmarkRequest;
import com.commit.campus.entity.Bookmark;
import com.commit.campus.repository.BookmarkRepository;
import com.commit.campus.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    @Autowired
    public BookmarkServiceImpl(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    @Override
    public void saveBookmark(BookmarkRequest bookmarkRequest) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(bookmarkRequest.getUserId());
        bookmark.setCampId(bookmarkRequest.getCampId());
        bookmark.setCreatedBookmarkDate(bookmarkRequest.getCreatedBookmarkDate());
        bookmark.setCampingInfo(bookmarkRequest.getCampInfo());
        bookmarkRepository.save(bookmark);
    }
}
