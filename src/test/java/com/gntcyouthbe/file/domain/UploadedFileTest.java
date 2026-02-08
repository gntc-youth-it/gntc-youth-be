package com.gntcyouthbe.file.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
