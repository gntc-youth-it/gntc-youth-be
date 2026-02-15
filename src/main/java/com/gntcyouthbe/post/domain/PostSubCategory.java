package com.gntcyouthbe.post.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostSubCategory {
    RETREAT_2026_WINTER("2026 겨울 수련회 (새 힘을 바라보라)", PostCategory.RETREAT);

    private final String displayName;
    private final PostCategory category;
}
