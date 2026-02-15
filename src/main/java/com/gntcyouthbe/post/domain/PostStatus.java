package com.gntcyouthbe.post.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostStatus {
    DRAFT("임시저장"),
    PENDING_REVIEW("검수대기"),
    APPROVED("승인"),
    REJECTED("반려");

    private final String displayName;
}
