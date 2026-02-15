package com.gntcyouthbe.post.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostCategory {
    RETREAT("수련회");

    private final String displayName;
}
