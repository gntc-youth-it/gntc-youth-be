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
}
