package com.gntcyouthbe.post.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.post.domain.PostSubCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostSubCategoryResponseTest {

    @Test
    @DisplayName("from()은 세부 카테고리의 이름, 표시명, 이미지, 시작일, 종료일을 매핑한다")
    void from_mapsAllFields() {
        // when
        PostSubCategoryResponse response = PostSubCategoryResponse.from(PostSubCategory.RETREAT_2026_WINTER, null);

        // then
        assertThat(response.name()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER.name());
        assertThat(response.displayName()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER.getDisplayName());
        assertThat(response.imageUrl()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER.getImageUrl());
        assertThat(response.startDate()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER.getStartDate());
        assertThat(response.endDate()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER.getEndDate());
    }

    @Test
    @DisplayName("날짜가 없는 세부 카테고리는 날짜와 이미지가 null이다")
    void from_nullableDateFields() {
        // when
        PostSubCategoryResponse response = PostSubCategoryResponse.from(PostSubCategory.NONE, null);

        // then
        assertThat(response.name()).isEqualTo("NONE");
        assertThat(response.displayName()).isEqualTo("기타");
        assertThat(response.imageUrl()).isNull();
        assertThat(response.startDate()).isNull();
        assertThat(response.endDate()).isNull();
        assertThat(response.verse()).isNull();
    }
}
