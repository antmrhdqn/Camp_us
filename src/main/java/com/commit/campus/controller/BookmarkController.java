package com.commit.campus.controller;

import com.commit.campus.dto.BookmarkRequest;
import com.commit.campus.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Autowired
    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping
    public ResponseEntity<Void> saveBookmark(@RequestBody BookmarkRequest bookmarkRequest) {
        bookmarkService.saveBookmark(bookmarkRequest);
        return ResponseEntity.ok().build();
    }

}
