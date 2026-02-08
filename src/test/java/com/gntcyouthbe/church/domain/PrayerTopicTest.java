package com.gntcyouthbe.church.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrayerTopicTest {

    @Test
    @DisplayName("PrayerTopic 생성 성공")
    void create() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);

        // when
        PrayerTopic topic = new PrayerTopic(churchInfo, "교회의 부흥을 위해", 1);

        // then
        assertThat(topic.getChurchInfo()).isEqualTo(churchInfo);
        assertThat(topic.getContent()).isEqualTo("교회의 부흥을 위해");
        assertThat(topic.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("기도제목 내용 수정 성공")
    void updateContent() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.SUWON);
        PrayerTopic topic = new PrayerTopic(churchInfo, "기존 기도제목", 1);

        // when
        topic.updateContent("수정된 기도제목");

        // then
        assertThat(topic.getContent()).isEqualTo("수정된 기도제목");
    }

    @Test
    @DisplayName("기도제목 순서 변경 성공")
    void updateSortOrder() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANSAN);
        PrayerTopic topic = new PrayerTopic(churchInfo, "기도제목", 1);

        // when
        topic.updateSortOrder(3);

        // then
        assertThat(topic.getSortOrder()).isEqualTo(3);
    }
}
