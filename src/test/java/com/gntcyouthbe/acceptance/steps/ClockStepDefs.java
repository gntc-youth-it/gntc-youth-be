package com.gntcyouthbe.acceptance.steps;

import com.gntcyouthbe.acceptance.support.TestClock;
import io.cucumber.java.ko.먼저;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ClockStepDefs {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TestClock testClock;

    public ClockStepDefs(TestClock testClock) {
        this.testClock = testClock;
    }

    @먼저("지금은 한국 시각으로 {string}이다")
    public void 지금은_한국_시각으로(String dateTime) {
        testClock.fixAt(LocalDateTime.parse(dateTime, DATE_TIME).atZone(KST).toInstant());
    }

    @먼저("지금은 UTC로 {string}이다")
    public void 지금은_UTC로(String dateTime) {
        testClock.fixAt(LocalDateTime.parse(dateTime, DATE_TIME).toInstant(ZoneOffset.UTC));
    }
}
