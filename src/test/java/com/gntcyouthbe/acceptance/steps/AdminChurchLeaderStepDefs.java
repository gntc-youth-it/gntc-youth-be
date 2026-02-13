package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AdminChurchApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import org.springframework.http.HttpStatus;

public class AdminChurchLeaderStepDefs {

    private final World world;
    private final AdminChurchApi adminChurchApi;

    public AdminChurchLeaderStepDefs(World world, AdminChurchApi adminChurchApi) {
        this.world = world;
        this.adminChurchApi = adminChurchApi;
    }

    @만일("마스터가 {string} 성전의 회장을 조회한다")
    public void 마스터가_성전의_회장을_조회한다(String churchId) {
        world.response = adminChurchApi.getChurchLeader(world.authToken, churchId);
    }

    @만일("사용자가 {string} 성전의 회장을 조회한다")
    public void 사용자가_성전의_회장을_조회한다(String churchId) {
        world.response = adminChurchApi.getChurchLeader(world.authToken, churchId);
    }

    @만일("인증 없이 {string} 성전의 회장을 조회한다")
    public void 인증_없이_성전의_회장을_조회한다(String churchId) {
        world.response = adminChurchApi.getChurchLeaderWithoutAuth(churchId);
    }

    @그러면("회장 정보가 반환된다")
    public void 회장_정보가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("ANYANG");
        assertThat(world.response.jsonPath().getString("churchName")).isEqualTo("안양");
        assertThat(world.response.jsonPath().getString("leader.name")).isEqualTo("리더유저");
    }

    @그러면("회장 없음 응답이 반환된다")
    public void 회장_없음_응답이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("SUWON");
        assertThat(world.response.jsonPath().getString("churchName")).isEqualTo("수원");
        assertThat((Object) world.response.jsonPath().getJsonObject("leader")).isNull();
    }
}
