package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksEmoji;

// 감사별·반응별 개수. ThanksReactionRepository.countByEntryIds의 결과다.
public record ThanksReactionCount(Long entryId, ThanksEmoji emoji, Long count) {
}
