package com.gntcyouthbe.cell.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CellRole {
    LEADER("구역장"),
    MEMBER("구역원");

    private final String displayName;
}
