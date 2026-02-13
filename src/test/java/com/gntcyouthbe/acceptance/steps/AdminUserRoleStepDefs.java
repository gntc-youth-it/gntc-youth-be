package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AdminUserApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;
import org.springframework.http.HttpStatus;

public class AdminUserRoleStepDefs {

    private final World world;
    private final AdminUserApi adminUserApi;

    public AdminUserRoleStepDefs(World world, AdminUserApi adminUserApi) {
        this.world = world;
        this.adminUserApi = adminUserApi;
    }

    @만일("마스터가 사용자 {long}의 역할을 {string}로 변경한다")
    public void 마스터가_사용자의_역할을_변경한다(long userId, String role) {
        world.response = adminUserApi.updateUserRole(world.authToken, userId, role);
    }

    @만일("사용자가 사용자 {long}의 역할을 {string}로 변경한다")
    public void 사용자가_사용자의_역할을_변경한다(long userId, String role) {
        world.response = adminUserApi.updateUserRole(world.authToken, userId, role);
    }

    @만일("인증 없이 사용자 {long}의 역할을 {string}로 변경한다")
    public void 인증_없이_사용자의_역할을_변경한다(long userId, String role) {
        world.response = adminUserApi.updateUserRoleWithoutAuth(userId, role);
    }

    @그러면("역할 변경이 성공한다")
    public void 역할_변경이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @그리고("변경된 역할이 {string}이다")
    public void 변경된_역할이_이다(String role) {
        assertThat(world.response.jsonPath().getString("role")).isEqualTo(role);
    }

    @그리고("기존 회장이 강등되어 반환된다")
    public void 기존_회장이_강등되어_반환된다() {
        assertThat(world.response.jsonPath().getString("previousLeader.name")).isEqualTo("리더유저");
        assertThat(world.response.jsonPath().getString("previousLeader.role")).isEqualTo("USER");
    }
}
