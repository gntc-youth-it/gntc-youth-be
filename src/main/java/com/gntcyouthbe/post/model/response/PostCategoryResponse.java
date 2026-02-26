package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.post.domain.PostCategory;
import java.util.Arrays;
import java.util.List;

public record PostCategoryResponse(String name, String displayName) {

    public static PostCategoryResponse from(PostCategory category) {
        return new PostCategoryResponse(category.name(), category.getDisplayName());
    }

    public static List<PostCategoryResponse> fromAll() {
        return Arrays.stream(PostCategory.values())
                .map(PostCategoryResponse::from)
                .toList();
    }
}
