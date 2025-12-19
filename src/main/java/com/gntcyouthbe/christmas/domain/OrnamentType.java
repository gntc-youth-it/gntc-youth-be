package com.gntcyouthbe.christmas.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum OrnamentType {
    BALL_RED("ball_red"),
    BALL_GOLD("ball_gold"),
    BALL_BLUE("ball_blue"),
    GIFT("gift"),
    CANDY("candy"),
    BELL("bell"),
    SNOWFLAKE("snowflake"),
    STAR("star");

    @JsonValue
    private final String value;

    @JsonCreator
    public static OrnamentType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid ornament type: " + value));
    }
}
