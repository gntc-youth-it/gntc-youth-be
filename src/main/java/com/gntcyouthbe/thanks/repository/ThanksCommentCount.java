package com.gntcyouthbe.thanks.repository;

// 감사별 댓글 수. ThanksCommentRepository.countByEntryIds의 결과다.
public record ThanksCommentCount(Long entryId, Long count) {
}
