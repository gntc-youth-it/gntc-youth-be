package com.gntcyouthbe.thanks.model.response;

import java.util.List;

// 그 감사의 댓글 전체, 오래된 순. 달거나 지운 뒤에도 이 모양으로 돌려줘서 그사이 다른 사람이 단 댓글도 보인다.
public record ThanksCommentListResponse(
        Long entryId,
        List<ThanksCommentResponse> comments
) {
}
