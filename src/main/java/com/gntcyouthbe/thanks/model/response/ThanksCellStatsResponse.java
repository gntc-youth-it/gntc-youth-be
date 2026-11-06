package com.gntcyouthbe.thanks.model.response;

import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.AuthorInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 구역을 고른 사람 전부가 나온다. 순서는 정하지 않고, 정렬과 합계, 최근 14일 계산은 FE가 한다.
public record ThanksCellStatsResponse(
        Long cellId,
        List<MemberStats> members
) {

    public record MemberStats(
            AuthorInfo user,
            // 가장 최근에 올린 감사의 createdAt이다. 안 올렸으면 null이다
            LocalDateTime lastPostedAt,
            // 올린 날만 담는다. 안 올렸으면 비어 있다
            List<DailyCount> dailyCounts
    ) {
    }

    // date는 감사의 missionDate다
    public record DailyCount(
            LocalDate date,
            long count
    ) {
    }
}
