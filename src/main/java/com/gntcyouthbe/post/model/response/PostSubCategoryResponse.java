package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.time.LocalDate;
import java.util.List;

public record PostSubCategoryResponse(
        String name,
        String displayName,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        VerseInfo verse,
        List<ChildInfo> children
) {

    public record ChildInfo(
            String name,
            String displayName
    ) {
        public static ChildInfo from(PostSubCategory subCategory) {
            return new ChildInfo(subCategory.name(), subCategory.getDisplayName());
        }
    }

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
                verse != null ? VerseInfo.from(verse) : null,
                subCategory.getChildren().stream().map(ChildInfo::from).toList()
        );
    }
}
