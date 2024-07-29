package com.commit.campus.controller;

import com.commit.campus.dto.BookmarkDTO;
import com.commit.campus.dto.BookmarkRequest;
import com.commit.campus.service.BookmarkService;
import com.commit.campus.view.BookmarkView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookmarkView>> getBookmarkByUserId(@PathVariable Long userId) {
        List<BookmarkDTO> sortedBookmarkDTOs = bookmarkService.getBookmark(userId).stream()
                .sorted((b1, b2) -> b2.getCreatedBookmarkDate().compareTo(b1.getCreatedBookmarkDate()))
                .collect(Collectors.toList());

        List<BookmarkView> bookmarkViews = sortedBookmarkDTOs.stream()
                .map(bookmarkDTO -> BookmarkView.builder()
                                                .userId(bookmarkDTO.getUserId())
                                                .campId(bookmarkDTO.getCampId())
                                                .build())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(bookmarkViews);
    }

}
