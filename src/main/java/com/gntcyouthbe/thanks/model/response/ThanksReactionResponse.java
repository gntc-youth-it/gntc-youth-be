package com.gntcyouthbe.thanks.model.response;

import com.gntcyouthbe.thanks.domain.ThanksEmoji;

// 감사 하나에 달린 반응 하나. 개수가 0인 반응은 넣지 않고, reactedByMe는 요청한 사람 기준이다.
public record ThanksReactionResponse(
        ThanksEmoji emoji,
        long count,
        boolean reactedByMe
) {
}
