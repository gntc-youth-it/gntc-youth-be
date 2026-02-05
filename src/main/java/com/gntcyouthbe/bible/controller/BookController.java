package com.gntcyouthbe.bible.controller;

import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse;
import com.gntcyouthbe.bible.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BookListResponse> getBookList() {
        return ResponseEntity.ok(bookService.getBookList());
    }

    @GetMapping("/{bookName}")
    public ResponseEntity<ChapterListResponse> getChapterList(
            @Valid @PathVariable @NotNull BookName bookName
    ) {
        return ResponseEntity.ok(bookService.getChapterList(bookName));
    }

    @GetMapping("/{bookName}/{chapter}")
    public ResponseEntity<ChapterResponse> getChapterVerses(
            @Valid @PathVariable @NotNull BookName bookName,
            @Valid @PathVariable @Min(1) int chapter
    ) {
        return ResponseEntity.ok(bookService.getChapterVerses(bookName, chapter));
    }
}
