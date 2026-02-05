package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AdventApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import org.springframework.http.HttpStatus;

public class AdventStepDefs {

    private final World world;
    private final AdventApi adventApi;

    public AdventStepDefs(World world, AdventApi adventApi) {
        this.world = world;
        this.adventApi = adventApi;
    }

    @만일("사용자가 이름 {string}, 성전 {string}, 기수 {int}로 강림절 말씀을 조회한다")
    public void 사용자가_강림절_말씀을_조회한다(String name, String temple, int batch) {
        world.response = adventApi.getAdventVerses(name, temple, batch);
    }

    @그러면("강림절 말씀 목록이 반환된다")
    public void 강림절_말씀_목록이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @그러면("Not Found 에러가 반환된다")
    public void not_found_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @만일("사용자가 파라미터 없이 강림절 말씀을 조회한다")
    public void 사용자가_파라미터_없이_강림절_말씀을_조회한다() {
        world.response = adventApi.getAdventVersesWithoutParams();
    }
}
