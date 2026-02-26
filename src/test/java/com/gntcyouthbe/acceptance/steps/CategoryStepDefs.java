package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.PostApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import java.time.LocalDate;
import java.util.List;
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

        List<String> names = world.response.jsonPath().getList("name", String.class);
        assertThat(names).isNotEmpty();

        // 모든 수련회 세부 카테고리에 포스터, 시작일, 종료일이 있어야 한다
        int size = names.size();
        for (int i = 0; i < size; i++) {
            assertThat(world.response.jsonPath().getString("[" + i + "].displayName")).isNotBlank();
            assertThat(world.response.jsonPath().getString("[" + i + "].imageUrl")).isNotBlank();
            assertThat(world.response.jsonPath().getString("[" + i + "].startDate")).isNotBlank();
            assertThat(world.response.jsonPath().getString("[" + i + "].endDate")).isNotBlank();
        }
    }

    @그러면("세부 카테고리가 시작일 기준 최신순으로 정렬되어 있다")
    public void 세부_카테고리가_시작일_기준_최신순으로_정렬되어_있다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<String> startDates = world.response.jsonPath().getList("startDate", String.class);
        assertThat(startDates).isNotEmpty();

        for (int i = 0; i < startDates.size() - 1; i++) {
            LocalDate current = LocalDate.parse(startDates.get(i));
            LocalDate next = LocalDate.parse(startDates.get(i + 1));
            assertThat(current).isAfterOrEqualTo(next);
        }
    }
}
