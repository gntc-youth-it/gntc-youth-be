package com.gntcyouthbe.post.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostSubCategoryResponseTest {

    @Test
    @DisplayName("from()은 세부 카테고리의 이름, 표시명, 이미지, 시작일, 종료일을 매핑한다")
    void from_mapsAllFields() {
        // when
        PostSubCategoryResponse response = PostSubCategoryResponse.from(PostSubCategory.RETREAT_2026_WINTER);

        // then
        assertThat(response.name()).isEqualTo("RETREAT_2026_WINTER");
        assertThat(response.displayName()).isEqualTo("2026 겨울 수련회 (새 힘을 얻으라)");
        assertThat(response.imageUrl()).isEqualTo("assets/2026-winter-poster.webp");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 1, 29));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    @DisplayName("날짜가 없는 세부 카테고리는 날짜와 이미지가 null이다")
    void from_nullableDateFields() {
        // when
        PostSubCategoryResponse response = PostSubCategoryResponse.from(PostSubCategory.NONE);

        // then
        assertThat(response.name()).isEqualTo("NONE");
        assertThat(response.displayName()).isEqualTo("기타");
        assertThat(response.imageUrl()).isNull();
        assertThat(response.startDate()).isNull();
        assertThat(response.endDate()).isNull();
    }

    @Test
    @DisplayName("수련회 카테고리의 세부 카테고리를 조회하면 시작일 기준 최신순으로 정렬된다")
    void fromCategory_retreat_sortedByStartDateDesc() {
        // when
        List<PostSubCategoryResponse> responses = PostSubCategoryResponse.fromCategory(PostCategory.RETREAT);

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses).allSatisfy(r -> {
            assertThat(r.imageUrl()).isNotNull();
            assertThat(r.startDate()).isNotNull();
            assertThat(r.endDate()).isNotNull();
        });

        // 시작일 기준 내림차순 정렬 검증
        assertThat(responses).isSortedAccordingTo(
                Comparator.comparing(PostSubCategoryResponse::startDate).reversed());
    }

    @Test
    @DisplayName("NONE 카테고리를 조회하면 NONE 세부 카테고리가 포함된 목록이 반환된다")
    void fromCategory_none_returnsNoneSubCategoryList() {
        // when
        List<PostSubCategoryResponse> responses = PostSubCategoryResponse.fromCategory(PostCategory.NONE);

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.getFirst().name()).isEqualTo("NONE");
    }
}
