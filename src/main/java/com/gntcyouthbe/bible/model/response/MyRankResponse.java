package com.gntcyouthbe.bible.model.response;

import java.time.Instant;
import java.time.ZonedDateTime;

public record MyRankResponse(
        Long userId,
        long count,
        Integer rank,
        long totalContributors,
        ZonedDateTime periodStartKst,
        ZonedDateTime periodEndKst,
        Instant startUtc,
        Instant endUtc,
        String timezone
) {}
