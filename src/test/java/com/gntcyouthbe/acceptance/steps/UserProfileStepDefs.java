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
        world.response = userProfileApi.saveProfile(
                world.authToken, "홍길동", "ANYANG", 45, "010-1234-5678", "MALE");
    }

    @그러면("프로필 저장이 성공한다")
    public void 프로필_저장이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("name")).isEqualTo("홍길동");
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("ANYANG");
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(45);
        assertThat(world.response.jsonPath().getString("phoneNumber")).isEqualTo("010-1234-5678");
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo("MALE");
    }

    @만일("사용자가 프로필을 조회한다")
    public void 사용자가_프로필을_조회한다() {
        world.response = userProfileApi.getProfile(world.authToken);
    }

    @그러면("프로필 조회가 성공한다")
    public void 프로필_조회가_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("name")).isEqualTo("홍길동");
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("ANYANG");
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(45);
        assertThat(world.response.jsonPath().getString("phoneNumber")).isEqualTo("010-1234-5678");
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo("MALE");
    }

    @만일("사용자가 프로필을 수정한다")
    public void 사용자가_프로필을_수정한다() {
        world.response = userProfileApi.saveProfile(
                world.authToken, "김철수", "SUWON", 46, "010-9876-5432", "FEMALE");
    }

    @그러면("프로필 수정이 성공한다")
    public void 프로필_수정이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("name")).isEqualTo("김철수");
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("SUWON");
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(46);
        assertThat(world.response.jsonPath().getString("phoneNumber")).isEqualTo("010-9876-5432");
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo("FEMALE");
    }

    @그러면("기본 정보만 반환된다")
    public void 기본_정보만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("generation")).isNull();
        assertThat(world.response.jsonPath().getString("phoneNumber")).isNull();
        assertThat(world.response.jsonPath().getString("gender")).isNull();
    }

    @만일("인증되지 않은 사용자가 프로필을 저장한다")
    public void 인증되지_않은_사용자가_프로필을_저장한다() {
        world.response = userProfileApi.saveProfileWithoutAuth(
                "홍길동", "ANYANG", 45, "010-1234-5678", "MALE");
    }

    @그러면("응답에 이름과 성전 정보가 포함된다")
    public void 응답에_이름과_성전_정보가_포함된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("name")).isEqualTo("홍길동");
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("ANYANG");
        assertThat(world.response.jsonPath().getString("churchName")).isEqualTo("안양");
    }

    @그러면("수정된 이름과 성전 정보가 반영된다")
    public void 수정된_이름과_성전_정보가_반영된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("name")).isEqualTo("김철수");
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("SUWON");
        assertThat(world.response.jsonPath().getString("churchName")).isEqualTo("수원");
    }
}
