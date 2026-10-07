package com.gntcyouthbe.zoo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 값 이름은 프론트와 같아야 한다.
// 운영 DB(PostgreSQL)에는 이 값들로 CHECK 제약이 생기고 ddl-auto: update는 제약을 고치지 않으므로, 값을 추가하면 제약도 직접 바꿔야 한다.
@RequiredArgsConstructor
@Getter
public enum ZooStop {
    AFRICA_1("제1아프리카관"),
    AUSTRALIA("호주관"),
    BIG_ANIMAL("대동물관"),
    BEAR("곰사"),
    PREDATOR("맹수사"),
    AFRICA_3("제3아프리카관"),
    AFRICA_2("제2아프리카관");

    private final String displayName;
}
