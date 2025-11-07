package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.model.response.DailyRankResponse;
import com.gntcyouthbe.bible.model.response.RankResponse;
import com.gntcyouthbe.bible.model.response.RecentRankResponse;
import com.gntcyouthbe.bible.model.response.WeeklyRankResponse;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final VerseCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public RankResponse getRank() {
        var topCopies = copyRepository.findTop5UsersByCopiesBetween(Instant.EPOCH, Instant.now());
        return new RankResponse(topCopies);
    }

    @Transactional(readOnly = true)
    public RecentRankResponse getRecentRank() {
        List<VerseCopy> recentCopies = copyRepository.findLatestPerUserOrderByCreatedAtDescLimited();
        return new RecentRankResponse(recentCopies);
    }

    @Transactional(readOnly = true)
    public DailyRankResponse getDailyRank() {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime startKst = nowKst.toLocalDate().atStartOfDay(KST);
        ZonedDateTime endKst = startKst.plusDays(1);
        var dailyTopCopies = copyRepository.findTop5UsersByCopiesBetween(startKst.toInstant(), endKst.toInstant());
        return new DailyRankResponse(nowKst, dailyTopCopies);
    }

    @Transactional(readOnly = true)
    public WeeklyRankResponse getWeeklyRank() {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime startOfWeek = nowKst.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(KST);
        ZonedDateTime endOfWeek = startOfWeek.plusWeeks(1);

        var top = copyRepository.findTop5UsersByCopiesBetween(
                startOfWeek.toInstant(), endOfWeek.toInstant());

        return new WeeklyRankResponse(startOfWeek, endOfWeek, top);
    }
}
