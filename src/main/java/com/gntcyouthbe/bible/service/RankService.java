package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.model.response.*;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
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
    public MyRankResponse getMyRank(final UserPrincipal userPrincipal) {
        ZonedDateTime startKst = ZonedDateTime.ofInstant(Instant.EPOCH, KST);
        ZonedDateTime endKst = ZonedDateTime.now(KST);
        return getMyPeriodRank(userPrincipal, startKst, endKst);
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
    public MyRankResponse getMyDailyRank(final UserPrincipal userPrincipal) {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime startKst = nowKst.toLocalDate().atStartOfDay(KST);
        ZonedDateTime endKst = startKst.plusDays(1);
        return getMyPeriodRank(userPrincipal, startKst, endKst);
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

    @Transactional(readOnly = true)
    public MyRankResponse getMyWeeklyRank(final UserPrincipal userPrincipal) {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        ZonedDateTime startKst = nowKst.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(KST);
        ZonedDateTime endKst = startKst.plusWeeks(1);
        return getMyPeriodRank(userPrincipal, startKst, endKst);
    }


    private MyRankResponse getMyPeriodRank(final UserPrincipal userPrincipal,
                                           final ZonedDateTime startKst,
                                           final ZonedDateTime endKst) {
        Instant startUtc = startKst.toInstant();
        Instant endUtc = endKst.toInstant();

        var opt = copyRepository.findUserRankBetween(userPrincipal.getUserId(), startUtc, endUtc);

        long total = copyRepository.countDistinctUsersBetween(startUtc, endUtc);

        if (opt.isPresent()) {
            var r = opt.get();
            return new MyRankResponse(
                    userPrincipal.getUserId(),
                    r.getCount() == null ? 0L : r.getCount(),
                    r.getRank(),                 // 동률은 동일 순위
                    r.getTotalContributors() == null ? total : r.getTotalContributors(),
                    startKst, endKst,
                    startUtc, endUtc,
                    "Asia/Seoul"
            );
        } else {
            return new MyRankResponse(
                    userPrincipal.getUserId(), 0L, null, total,
                    startKst, endKst,
                    startUtc, endUtc,
                    "Asia/Seoul"
            );
        }
    }
}
