package com.gntcyouthbe.thanks.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ThanksSlotTest {

    @ParameterizedTest(name = "{0}은 {1}")
    @CsvSource({
            "06:00, MORNING",
            "11:59:59.999999999, MORNING",
            "12:00, AFTERNOON",
            "17:59:59.999999999, AFTERNOON",
            "18:00, EVENING",
            "23:59:59.999999999, EVENING"
    })
    @DisplayName("시각에 따라 열린 칸이 정해진다")
    void openAt(LocalTime time, ThanksSlot expected) {
        assertThat(ThanksSlot.openAt(time)).contains(expected);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"00:00", "03:00", "05:59:59.999999999"})
    @DisplayName("00:00–05:59에는 열린 칸이 없다")
    void openAt_closed(LocalTime time) {
        assertThat(ThanksSlot.openAt(time)).isEmpty();
    }
}
