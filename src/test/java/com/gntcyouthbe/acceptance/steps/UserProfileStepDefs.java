package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.UserProfileApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import org.springframework.http.HttpStatus;

public class UserProfileStepDefs {

    private final World world;
    private final UserProfileApi userProfileApi;
    private final AuthApi authApi;

    public UserProfileStepDefs(World world, UserProfileApi userProfileApi, AuthApi authApi) {
        this.world = world;
        this.userProfileApi = userProfileApi;
        this.authApi = authApi;
    }

    @만일("사용자가 프로필을 저장한다")
    public void 사용자가_프로필을_저장한다() {
        world.response = userProfileApi.saveProfile(world.authToken, 45, "010-1234-5678", "MALE");
    }

    @그러면("프로필 저장이 성공한다")
    public void 프로필_저장이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(45);
        assertThat(world.response.jsonPath().getString("phone_number")).isEqualTo("010-1234-5678");
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo("MALE");
    }

    @만일("사용자가 프로필을 조회한다")
    public void 사용자가_프로필을_조회한다() {
        world.response = userProfileApi.getProfile(world.authToken);
    }

    @그러면("프로필 조회가 성공한다")
    public void 프로필_조회가_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(45);
        assertThat(world.response.jsonPath().getString("phone_number")).isEqualTo("010-1234-5678");
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo("MALE");
    }

    @만일("사용자가 프로필을 수정한다")
    public void 사용자가_프로필을_수정한다() {
        world.response = userProfileApi.saveProfile(world.authToken, 46, "010-9876-5432", "FEMALE");
    }

    @그러면("프로필 수정이 성공한다")
    public void 프로필_수정이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(46);
        assertThat(world.response.jsonPath().getString("phone_number")).isEqualTo("010-9876-5432");
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo("FEMALE");
    }

    @만일("인증되지 않은 사용자가 프로필을 저장한다")
    public void 인증되지_않은_사용자가_프로필을_저장한다() {
        world.response = userProfileApi.saveProfileWithoutAuth(45, "010-1234-5678", "MALE");
    }
}
