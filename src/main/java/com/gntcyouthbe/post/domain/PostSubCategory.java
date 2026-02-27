package com.gntcyouthbe.post.domain;

import com.gntcyouthbe.bible.domain.BookName;
import java.time.LocalDate;

import lombok.Getter;

@Getter
public enum PostSubCategory {
    RETREAT_2026_WINTER("2026 겨울 수련회 (새 힘을 얻으라)", PostCategory.RETREAT,
            LocalDate.of(2026, 1, 29), LocalDate.of(2026, 1, 31), "assets/2026-winter-poster.webp",
            BookName.ISAIAH, 40, 31),
    NONE("기타", PostCategory.NONE);

    private final String displayName;
    private final PostCategory category;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String imageUrl;
    private final BookName bookName;
    private final Integer chapter;
    private final Integer verseNumber;

    PostSubCategory(String displayName, PostCategory category, LocalDate startDate, LocalDate endDate,
            String imageUrl, BookName bookName, Integer chapter, Integer verseNumber) {
        this.displayName = displayName;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.bookName = bookName;
        this.chapter = chapter;
        this.verseNumber = verseNumber;
    }

    PostSubCategory(String displayName, PostCategory category) {
        this(displayName, category, null, null, null, null, null, null);
    }

    public boolean hasVerse() {
        return bookName != null;
    }
}
