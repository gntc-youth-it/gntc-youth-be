package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AdminUserApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class AdminUserStepDefs {

    private final World world;
    private final AdminUserApi adminUserApi;
    private final List<Integer> previousPageUserIds = new ArrayList<>();

    public AdminUserStepDefs(World world, AdminUserApi adminUserApi) {
        this.world = world;
        this.adminUserApi = adminUserApi;
    }

    @만일("마스터가 전체 사용자 목록을 조회한다")
    public void 마스터가_전체_사용자_목록을_조회한다() {
        world.response = adminUserApi.getUsers(world.authToken, 0, 10);
    }

    @만일("사용자가 전체 사용자 목록을 조회한다")
    public void 사용자가_전체_사용자_목록을_조회한다() {
        world.response = adminUserApi.getUsers(world.authToken, 0, 10);
    }

    @만일("인증 없이 전체 사용자 목록을 조회한다")
    public void 인증_없이_전체_사용자_목록을_조회한다() {
        world.response = adminUserApi.getUsersWithoutAuth();
    }

    @만일("마스터가 이름 {string}으로 사용자를 검색한다")
    public void 마스터가_이름으로_사용자를_검색한다(String name) {
        world.response = adminUserApi.getUsersByName(world.authToken, 0, 10, name);
    }

    @그러면("전체 사용자 목록이 반환된다")
    public void 전체_사용자_목록이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String, Object>> users = world.response.jsonPath().getList("users");
        assertThat(users).hasSize(5);

        int totalElements = world.response.jsonPath().getInt("totalElements");
        assertThat(totalElements).isEqualTo(5);

        // 각 사용자에게 필수 필드가 존재하는지 검증
        for (Map<String, Object> user : users) {
            assertThat(user).containsKey("userId");
            assertThat(user).containsKey("name");
            assertThat(user).containsKey("churchId");
            assertThat(user).containsKey("churchName");
            assertThat(user).containsKey("generation");
            assertThat(user).containsKey("phoneNumber");
            assertThat(user).containsKey("role");
        }
    }

    @그러면("프로필이 있는 사용자의 전화번호가 마스킹되어 반환된다")
    public void 프로필이_있는_사용자의_전화번호가_마스킹되어_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String, Object>> users = world.response.jsonPath().getList("users");

        // 리더유저 (프로필이 있는 사용자) 찾기
        Map<String, Object> leaderUser = users.stream()
                .filter(u -> "리더유저".equals(u.get("name")))
                .findFirst()
                .orElseThrow();

        assertThat(leaderUser.get("userId")).isNotNull();
        assertThat(leaderUser.get("phoneNumber")).isEqualTo("010-****-5678");
        assertThat(leaderUser.get("generation")).isEqualTo(45);
        assertThat(leaderUser.get("churchId")).isEqualTo("ANYANG");
        assertThat(leaderUser.get("churchName")).isEqualTo("안양");
        assertThat(leaderUser.get("role")).isEqualTo("LEADER");
    }

    @그러면("프로필이 없는 사용자의 기수와 전화번호는 null이다")
    public void 프로필이_없는_사용자의_기수와_전화번호는_null이다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String, Object>> users = world.response.jsonPath().getList("users");

        // 테스트유저 (프로필 없음, 성전 없음)
        Map<String, Object> testUser = users.stream()
                .filter(u -> "테스트유저".equals(u.get("name")))
                .findFirst()
                .orElseThrow();

        assertThat(testUser.get("generation")).isNull();
        assertThat(testUser.get("phoneNumber")).isNull();
        assertThat(testUser.get("churchId")).isNull();
        assertThat(testUser.get("churchName")).isNull();
        assertThat(testUser.get("role")).isEqualTo("USER");
    }

    @그러면("검색된 사용자 {int}명이 반환된다")
    public void 검색된_사용자_n명이_반환된다(int count) {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String, Object>> users = world.response.jsonPath().getList("users");
        assertThat(users).hasSize(count);

        int totalElements = world.response.jsonPath().getInt("totalElements");
        assertThat(totalElements).isEqualTo(count);
    }

    @만일("마스터가 사용자 목록을 페이지 {int} 사이즈 {int} 조건으로 조회한다")
    public void 마스터가_사용자_목록을_페이지_사이즈_조건으로_조회한다(int page, int size) {
        // 이전 페이지 userId 저장
        if (world.response != null && world.response.statusCode() == HttpStatus.OK.value()) {
            List<Map<String, Object>> prevUsers = world.response.jsonPath().getList("users");
            previousPageUserIds.clear();
            for (Map<String, Object> u : prevUsers) {
                previousPageUserIds.add((Integer) u.get("userId"));
            }
        }
        world.response = adminUserApi.getUsers(world.authToken, page, size);
    }

    @그러면("사용자 목록이 {int}명 반환되고 총 {int}명이다")
    public void 사용자_목록이_n명_반환되고_총_m명이다(int count, int total) {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Map<String, Object>> users = world.response.jsonPath().getList("users");
        assertThat(users).hasSize(count);

        int totalElements = world.response.jsonPath().getInt("totalElements");
        assertThat(totalElements).isEqualTo(total);
    }

    @그리고("이전 페이지와 현재 페이지에 중복된 사용자가 없다")
    public void 이전_페이지와_현재_페이지에_중복된_사용자가_없다() {
        List<Map<String, Object>> currentUsers = world.response.jsonPath().getList("users");
        List<Integer> currentUserIds = currentUsers.stream()
                .map(u -> (Integer) u.get("userId"))
                .toList();

        assertThat(currentUserIds).doesNotContainAnyElementsOf(previousPageUserIds);
    }

    @그러면("사용자 목록이 ID 내림차순으로 정렬되어 있다")
    public void 사용자_목록이_ID_내림차순으로_정렬되어_있다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Integer> userIds = world.response.jsonPath().getList("users.userId");
        assertThat(userIds).hasSizeGreaterThan(1);

        for (int i = 0; i < userIds.size() - 1; i++) {
            assertThat(userIds.get(i)).isGreaterThan(userIds.get(i + 1));
        }
    }
}
