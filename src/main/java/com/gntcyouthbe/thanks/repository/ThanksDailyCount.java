package com.gntcyouthbe.thanks.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 사람별·날짜별 올린 개수와 그날 가장 최근에 올린 시각. ThanksEntryRepository.countDailyByUserIds의 결과다.
public record ThanksDailyCount(Long userId, LocalDate missionDate, Long count, LocalDateTime lastPostedAt) {
}
