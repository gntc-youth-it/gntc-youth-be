package com.gntcyouthbe.bible.controller;

import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.RecentChapterResponse;
import com.gntcyouthbe.bible.service.BookService;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookListResponse> getBookList(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(bookService.getBookList(userPrincipal));
    }

    @GetMapping("/{bookName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChapterListResponse> getChapterList(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @PathVariable @NotNull BookName bookName
    ) {
        return ResponseEntity.ok(bookService.getChapterList(userPrincipal, bookName));
    }

    @GetMapping("/copy/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecentChapterResponse> getRecentChapter(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(bookService.getRecentChapter(userPrincipal));
    }
}
