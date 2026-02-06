package com.gntcyouthbe.church.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.model.response.ChurchListResponse;
import com.gntcyouthbe.church.model.response.ChurchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChurchServiceTest {

    private final ChurchService churchService = new ChurchService();

    @Test
    @DisplayName("getChurches - 모든 교회 목록을 반환한다")
    void getChurches() {
        // when
        ChurchListResponse response = churchService.getChurches();

        // then
        assertThat(response.getChurches()).hasSize(ChurchId.values().length);
    }

    @Test
    @DisplayName("getChurches - 교회 목록에 안양이 포함되어 있다")
    void getChurches_containsAnyang() {
        // when
        ChurchListResponse response = churchService.getChurches();

        // then
        assertThat(response.getChurches())
                .extracting(ChurchResponse::getCode)
                .contains("ANYANG");
        assertThat(response.getChurches())
                .extracting(ChurchResponse::getName)
                .contains("안양");
    }

    @Test
    @DisplayName("getChurches - 동일한 인스턴스를 반환한다 (캐싱)")
    void getChurches_returnsSameInstance() {
        // when
        ChurchListResponse first = churchService.getChurches();
        ChurchListResponse second = churchService.getChurches();

        // then
        assertThat(first).isSameAs(second);
    }
}
