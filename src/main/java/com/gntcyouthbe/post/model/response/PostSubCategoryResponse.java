package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.util.Arrays;
import java.util.List;

public record PostSubCategoryResponse(String name, String displayName) {

    public static PostSubCategoryResponse from(PostSubCategory subCategory) {
        return new PostSubCategoryResponse(subCategory.name(), subCategory.getDisplayName());
    }

    public static List<PostSubCategoryResponse> fromCategory(PostCategory category) {
        return Arrays.stream(PostSubCategory.values())
                .filter(sub -> sub.getCategory() == category)
                .map(PostSubCategoryResponse::from)
                .toList();
    }
}
