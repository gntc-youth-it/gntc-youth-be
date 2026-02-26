package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PostSubCategoryResponse(
        String name,
        String displayName,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate
) {

    private static final Map<PostCategory, List<PostSubCategoryResponse>> CACHED_SUB_CATEGORIES =
            Arrays.stream(PostSubCategory.values())
                    .collect(Collectors.groupingBy(
                            PostSubCategory::getCategory,
                            () -> new EnumMap<>(PostCategory.class),
                            Collectors.collectingAndThen(
                                    Collectors.mapping(PostSubCategoryResponse::from, Collectors.toList()),
                                    list -> list.stream()
                                            .sorted(Comparator.comparing(
                                                    PostSubCategoryResponse::startDate,
                                                    Comparator.nullsLast(Comparator.reverseOrder())))
                                            .toList()
                            )
                    ));

    public static PostSubCategoryResponse from(PostSubCategory subCategory) {
        return new PostSubCategoryResponse(
                subCategory.name(),
                subCategory.getDisplayName(),
                subCategory.getImageUrl(),
                subCategory.getStartDate(),
                subCategory.getEndDate()
        );
    }

    public static List<PostSubCategoryResponse> fromCategory(PostCategory category) {
        return CACHED_SUB_CATEGORIES.getOrDefault(category, List.of());
    }
}
