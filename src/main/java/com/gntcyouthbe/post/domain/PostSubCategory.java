package com.gntcyouthbe.post.domain;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public enum PostSubCategory {
    RETREAT_2026_WINTER("2026 겨울 수련회 (새 힘을 얻으라)", PostCategory.RETREAT,
            LocalDate.of(2026, 1, 29), LocalDate.of(2026, 1, 31), "assets/2026-winter-poster.webp"),
    NONE("기타", PostCategory.NONE);

    private final String displayName;
    private final PostCategory category;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String imageUrl;

    PostSubCategory(String displayName, PostCategory category, LocalDate startDate, LocalDate endDate,
            String imageUrl) {
        this.displayName = displayName;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
    }

    PostSubCategory(String displayName, PostCategory category) {
        this(displayName, category, null, null, null);
    }
}
