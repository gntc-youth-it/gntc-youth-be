package com.gntcyouthbe.thanks.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ThanksMediaTypeTest {

    @ParameterizedTest(name = "{0}는 {1}")
    @CsvSource({
            "image/webp, IMAGE",
            "image/jpeg, IMAGE",
            "IMAGE/PNG, IMAGE",
            "video/mp4, VIDEO",
            "video/quicktime, VIDEO"
    })
    @DisplayName("image/*는 IMAGE, video/*는 VIDEO다")
    void from(String contentType, ThanksMediaType expected) {
        assertThat(ThanksMediaType.from(contentType)).contains(expected);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"application/pdf", "text/plain", "audio/mpeg", "imagex/png"})
    @DisplayName("사진이나 영상이 아니면 종류가 없다")
    void from_notMedia(String contentType) {
        assertThat(ThanksMediaType.from(contentType)).isEmpty();
    }
}
