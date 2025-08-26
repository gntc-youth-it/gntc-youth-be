package com.gntcyouthbe.church.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChurchId {
    ANYANG("안양");

    private final String displayName;
}
