package com.gntcyouthbe.church.domain;

import com.gntcyouthbe.file.domain.UploadedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChurchInfoTest {

    @Test
    @DisplayName("ChurchInfo 생성 성공")
    void create() {
        // when
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);

        // then
        assertThat(churchInfo.getChurchId()).isEqualTo(ChurchId.ANYANG);
        assertThat(churchInfo.getGroupPhoto()).isNull();
    }

    @Test
    @DisplayName("단체사진 업데이트 성공")
    void updateGroupPhoto() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.SUWON);
        UploadedFile photo = new UploadedFile(
            "group.jpg", "stored.jpg", "/uploads/stored.jpg", "image/jpeg", 2048L
        );

        // when
        churchInfo.updateGroupPhoto(photo);

        // then
        assertThat(churchInfo.getGroupPhoto()).isEqualTo(photo);
    }

    @Test
    @DisplayName("단체사진을 null로 업데이트하면 사진 제거")
    void updateGroupPhoto_null() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANSAN);
        UploadedFile photo = new UploadedFile(
            "group.jpg", "stored.jpg", "/uploads/stored.jpg", "image/jpeg", 2048L
        );
        churchInfo.updateGroupPhoto(photo);

        // when
        churchInfo.updateGroupPhoto(null);

        // then
        assertThat(churchInfo.getGroupPhoto()).isNull();
    }
}
