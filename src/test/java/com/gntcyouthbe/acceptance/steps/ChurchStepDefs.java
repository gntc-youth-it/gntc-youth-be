package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ChurchApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import org.springframework.http.HttpStatus;

public class ChurchStepDefs {

    private final World world;
    private final ChurchApi churchApi;

    public ChurchStepDefs(World world, ChurchApi churchApi) {
        this.world = world;
        this.churchApi = churchApi;
    }

    @만일("사용자가 교회 목록을 조회한다")
    public void 사용자가_교회_목록을_조회한다() {
        world.response = churchApi.getChurches();
    }

    @그러면("교회 목록이 반환된다")
    public void 교회_목록이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("churches")).isNotEmpty();
    }
}
