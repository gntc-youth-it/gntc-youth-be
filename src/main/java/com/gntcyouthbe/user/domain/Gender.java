package com.gntcyouthbe.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {
    MALE("남"),
    FEMALE("여");

    private final String displayName;
}
