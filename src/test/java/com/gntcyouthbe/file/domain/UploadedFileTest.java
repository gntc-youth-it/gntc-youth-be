package com.gntcyouthbe.file.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class UploadedFileTest {

    @Test
    @DisplayName("UploadedFile 생성 성공")
    void create() {
        // when
        UploadedFile file = new UploadedFile(
            "photo.jpg",
            "550e8400-e29b-41d4-a716-446655440000.jpg",
            "/uploads/2025/01/550e8400-e29b-41d4-a716-446655440000.jpg",
            "image/jpeg",
            1024L
        );

        // then
        assertThat(file.getOriginalFilename()).isEqualTo("photo.jpg");
        assertThat(file.getStoredFilename()).isEqualTo("550e8400-e29b-41d4-a716-446655440000.jpg");
        assertThat(file.getFilePath()).isEqualTo("/uploads/2025/01/550e8400-e29b-41d4-a716-446655440000.jpg");
        assertThat(file.getContentType()).isEqualTo("image/jpeg");
        assertThat(file.getFileSize()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("presigned URL을 요청한 사용자(createdBy)가 올린 사람이다")
    void isUploadedBy() {
        // given
        UploadedFile file = createFile("image/jpeg");
        ReflectionTestUtils.setField(file, "createdBy", 1L);

        // then
        assertThat(file.isUploadedBy(1L)).isTrue();
        assertThat(file.isUploadedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("올린 사람이 기록되지 않은 파일은 누가 올린 것도 아니다")
    void isUploadedBy_unknownUploader() {
        assertThat(createFile("image/jpeg").isUploadedBy(1L)).isFalse();
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "image/jpeg, true",
            "image/webp, true",
            "IMAGE/PNG, true",
            "video/mp4, false",
            "application/pdf, false"
    })
    @DisplayName("content type이 image/*이면 이미지다")
    void isImage(String contentType, boolean expected) {
        assertThat(createFile(contentType).isImage()).isEqualTo(expected);
    }

    private UploadedFile createFile(String contentType) {
        return new UploadedFile("photo", "stored", "uploads/stored", contentType, 1024L);
    }
}
