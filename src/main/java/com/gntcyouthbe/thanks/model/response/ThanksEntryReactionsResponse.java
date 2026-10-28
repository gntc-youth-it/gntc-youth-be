package com.gntcyouthbe.thanks.model.response;

import java.util.List;

// 반응을 켜거나 끈 뒤 그 감사의 반응 전체. 그사이 다른 사람이 누른 것까지 반영한 최신 값이다.
public record ThanksEntryReactionsResponse(
        Long entryId,
        List<ThanksReactionResponse> reactions
) {
}
