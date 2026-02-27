package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.time.LocalDate;

public record PostSubCategoryResponse(
        String name,
        String displayName,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        VerseInfo verse
) {

    public record VerseInfo(
            String bookName,
            String bookDisplayName,
            int chapter,
            int verse,
            String content
    ) {
        public static VerseInfo from(Verse verse) {
            return new VerseInfo(
                    verse.getBookName().name(),
                    verse.getBookName().getDisplayName(),
                    verse.getChapter(),
                    verse.getNumber(),
                    verse.getContent()
            );
        }
    }

    public static PostSubCategoryResponse from(PostSubCategory subCategory, Verse verse) {
        return new PostSubCategoryResponse(
                subCategory.name(),
                subCategory.getDisplayName(),
                subCategory.getImageUrl(),
                subCategory.getStartDate(),
                subCategory.getEndDate(),
                verse != null ? VerseInfo.from(verse) : null
        );
    }
}
