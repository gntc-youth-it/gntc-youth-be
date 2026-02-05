package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.UserApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import org.springframework.http.HttpStatus;

public class UserStepDefs {

    private final World world;
    private final UserApi userApi;
    private final AuthApi authApi;

    public UserStepDefs(World world, UserApi userApi, AuthApi authApi) {
        this.world = world;
        this.userApi = userApi;
        this.authApi = authApi;
    }

    @먼저("사용자가 로그인되어 있다")
    public void 사용자가_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("test@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("사용자가 이름을 {string}로 변경한다")
    public void 사용자가_이름을_변경한다(String newName) {
        world.response = userApi.updateName(world.authToken, newName);
    }

    @그러면("이름 변경이 성공한다")
    public void 이름_변경이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("인증되지 않은 사용자가 이름을 {string}로 변경한다")
    public void 인증되지_않은_사용자가_이름을_변경한다(String newName) {
        world.response = userApi.updateNameWithoutAuth(newName);
    }

    @그러면("인증 에러가 반환된다")
    public void 인증_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
