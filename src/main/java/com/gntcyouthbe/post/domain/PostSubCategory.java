package com.gntcyouthbe.post.domain;

import com.gntcyouthbe.bible.domain.BookName;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public enum PostSubCategory {
    RETREAT_2026_SUMMER("2026 여름 수련회 (곧은 길로 행하라)", PostCategory.RETREAT,
            LocalDate.of(2026, 8, 13), LocalDate.of(2026, 8, 15), "assets/2026-summer-poster.webp",
            BookName.JOSHUA, 1, 7),
    RETREAT_2026_SUMMER_SPORTS("체육대회", RETREAT_2026_SUMMER),
    RETREAT_2026_SUMMER_WALK("함께걷장", RETREAT_2026_SUMMER),
    RETREAT_2026_SUMMER_ETC("그외 활동", RETREAT_2026_SUMMER),
    RETREAT_2026_WINTER("2026 겨울 수련회 (새 힘을 얻으라)", PostCategory.RETREAT,
            LocalDate.of(2026, 1, 29), LocalDate.of(2026, 1, 31), "assets/2026-winter-poster.webp",
            BookName.ISAIAH, 40, 31),
    RETREAT_2026_WINTER_PRESERVICE("예배 전 찬양", RETREAT_2026_WINTER),
    RETREAT_2026_WINTER_SPECIAL("특송&헌금송", RETREAT_2026_WINTER),
    RETREAT_2026_WINTER_SING("새 힘을 노래하라", RETREAT_2026_WINTER),
    NONE("기타", PostCategory.NONE);

    private final String displayName;
    private final PostCategory category;
    private final PostSubCategory parent;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String imageUrl;
    private final BookName bookName;
    private final Integer chapter;
    private final Integer verseNumber;

    PostSubCategory(String displayName, PostCategory category, LocalDate startDate, LocalDate endDate,
            String imageUrl, BookName bookName, Integer chapter, Integer verseNumber) {
        this(displayName, category, null, startDate, endDate, imageUrl, bookName, chapter, verseNumber);
    }

    PostSubCategory(String displayName, PostCategory category) {
        this(displayName, category, null, null, null, null, null, null, null);
    }

    PostSubCategory(String displayName, PostSubCategory parent) {
        this(displayName, parent.category, parent, null, null, null, null, null, null);
    }

    PostSubCategory(String displayName, PostCategory category, PostSubCategory parent,
            LocalDate startDate, LocalDate endDate, String imageUrl,
            BookName bookName, Integer chapter, Integer verseNumber) {
        this.displayName = displayName;
        this.category = category;
        this.parent = parent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.bookName = bookName;
        this.chapter = chapter;
        this.verseNumber = verseNumber;
    }

    public boolean hasVerse() {
        return bookName != null;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public List<PostSubCategory> getChildren() {
        return Arrays.stream(values())
                .filter(sub -> sub.parent == this)
                .toList();
    }

    public List<PostSubCategory> withChildren() {
        List<PostSubCategory> result = new ArrayList<>();
        result.add(this);
        result.addAll(getChildren());
        return result;
    }
}
