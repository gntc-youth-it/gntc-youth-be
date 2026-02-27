package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.PostApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class CategoryStepDefs {

    private final World world;
    private final PostApi postApi;

    public CategoryStepDefs(World world, PostApi postApi) {
        this.world = world;
        this.postApi = postApi;
    }

    @만일("수련회 세부 카테고리를 조회한다")
    public void 수련회_세부_카테고리를_조회한다() {
        world.response = postApi.getSubCategories("RETREAT");
    }

    @그러면("세부 카테고리에 포스터와 기간 정보가 포함되어 있다")
    public void 세부_카테고리에_포스터와_기간_정보가_포함되어_있다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String, Object>> responses = world.response.jsonPath().getList(".");
        assertThat(responses).isNotEmpty();

        assertThat(responses).allSatisfy(response -> {
            assertThat((String) response.get("displayName")).isNotBlank();
            assertThat((String) response.get("imageUrl")).isNotBlank();
            assertThat(response.get("startDate")).isNotNull();
            assertThat(response.get("endDate")).isNotNull();
        });
    }

    @그리고("세부 카테고리가 시작일 기준 최신순으로 정렬되어 있다")
    public void 세부_카테고리가_시작일_기준_최신순으로_정렬되어_있다() {
        List<String> startDates = world.response.jsonPath().getList("startDate", String.class);
        assertThat(startDates).isNotEmpty();

        List<LocalDate> dates = startDates.stream().map(LocalDate::parse).toList();
        assertThat(dates).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @SuppressWarnings("unchecked")
    @그리고("세부 카테고리에 말씀 정보가 포함되어 있다")
    public void 세부_카테고리에_말씀_정보가_포함되어_있다() {
        List<Map<String, Object>> responses = world.response.jsonPath().getList(".");

        Map<String, Object> retreat2026 = responses.stream()
                .filter(r -> "RETREAT_2026_WINTER".equals(r.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("RETREAT_2026_WINTER not found"));

        Map<String, Object> verse = (Map<String, Object>) retreat2026.get("verse");
        assertThat(verse).isNotNull();
        assertThat((String) verse.get("bookName")).isEqualTo("ISAIAH");
        assertThat((String) verse.get("bookDisplayName")).isEqualTo("이사야");
        assertThat((Integer) verse.get("chapter")).isEqualTo(40);
        assertThat((Integer) verse.get("verse")).isEqualTo(31);
        assertThat((String) verse.get("content")).isNotBlank();
    }
}
