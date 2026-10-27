package com.gntcyouthbe.acceptance.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

// 인수 테스트에서 서버의 지금 시각을 정할 수 있는 Clock. 운영 서버처럼 시간대는 UTC다.
// 시나리오가 끝나면 AcceptanceTestSupport가 실제 시각으로 되돌린다.
@Component
@Primary
public class TestClock extends Clock {

    private volatile Clock clock = Clock.systemUTC();

    public void fixAt(Instant instant) {
        clock = Clock.fixed(instant, ZoneOffset.UTC);
    }

    public void reset() {
        clock = Clock.systemUTC();
    }

    @Override
    public ZoneId getZone() {
        return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return clock.withZone(zone);
    }

    @Override
    public Instant instant() {
        return clock.instant();
    }
}
