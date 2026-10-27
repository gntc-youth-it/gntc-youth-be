package com.gntcyouthbe.thanks.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

// 붙인 파일의 content type으로 정한다. 1단계 FE는 사진만 올리지만 5단계 영상을 위해 지금부터 둘 다 받는다.
@RequiredArgsConstructor
public enum ThanksMediaType {
    IMAGE("image/"),
    VIDEO("video/");

    private final String contentTypePrefix;

    public static Optional<ThanksMediaType> from(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> normalized.startsWith(type.contentTypePrefix))
                .findFirst();
    }
}
