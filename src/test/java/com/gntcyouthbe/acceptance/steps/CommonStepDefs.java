package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import org.springframework.http.HttpStatus;

public class CommonStepDefs {

    private final World world;

    public CommonStepDefs(World world) {
        this.world = world;
    }

    @그러면("Bad Request 에러가 반환된다")
    public void bad_request_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @그러면("Not Found 에러가 반환된다")
    public void not_found_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @그러면("응답 상태 코드는 {int}이다")
    public void 응답_상태_코드는_이다(int statusCode) {
        assertThat(world.response.statusCode()).isEqualTo(statusCode);
    }

    @그러면("에러 코드는 {int}이다")
    public void 에러_코드는_이다(int code) {
        assertThat(world.response.jsonPath().getInt("code")).isEqualTo(code);
    }

    @그러면("에러 메시지는 {string}이다")
    public void 에러_메시지는_이다(String message) {
        assertThat(world.response.jsonPath().getString("message")).isEqualTo(message);
    }
}
