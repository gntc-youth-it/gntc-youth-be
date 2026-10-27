package com.gntcyouthbe.thanks.domain;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

// 하루 세 칸. 한국 시각 00:00–05:59에는 열린 칸이 없다.
@RequiredArgsConstructor
public enum ThanksSlot {
    MORNING(6, 12),
    AFTERNOON(12, 18),
    EVENING(18, 24);

    private final int startHour;
    private final int endHour;

    public static Optional<ThanksSlot> openAt(LocalTime time) {
        return Arrays.stream(values())
                .filter(slot -> slot.startHour <= time.getHour() && time.getHour() < slot.endHour)
                .findFirst();
    }
}
