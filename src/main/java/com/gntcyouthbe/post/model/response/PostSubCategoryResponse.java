package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PostSubCategoryResponse(String name, String displayName) {

    private static final Map<PostCategory, List<PostSubCategoryResponse>> CACHED_SUB_CATEGORIES =
            Arrays.stream(PostSubCategory.values())
                    .collect(Collectors.groupingBy(
                            PostSubCategory::getCategory,
                            () -> new EnumMap<>(PostCategory.class),
                            Collectors.mapping(PostSubCategoryResponse::from, Collectors.toList())
                    ));

    public static PostSubCategoryResponse from(PostSubCategory subCategory) {
        return new PostSubCategoryResponse(subCategory.name(), subCategory.getDisplayName());
    }

    public static List<PostSubCategoryResponse> fromCategory(PostCategory category) {
        return CACHED_SUB_CATEGORIES.getOrDefault(category, List.of());
    }
}
