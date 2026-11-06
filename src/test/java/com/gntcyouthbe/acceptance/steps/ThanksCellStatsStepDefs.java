package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ThanksApi;
import com.gntcyouthbe.acceptance.support.context.ThanksScenarioContext;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;

public class ThanksCellStatsStepDefs {

    private static final Long UNKNOWN_CELL_ID = 999_999L;

    private final World world;
    private final ThanksScenarioContext thanks;
    private final ThanksApi thanksApi;
    private final JdbcTemplate jdbcTemplate;

    public ThanksCellStatsStepDefs(World world, ThanksScenarioContext thanks, ThanksApi thanksApi, JdbcTemplate jdbcTemplate) {
        this.world = world;
        this.thanks = thanks;
        this.thanksApi = thanksApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- 요청 ---

    @만일("{string}가 구역 {string}의 현황을 조회한다")
    public void 구역_현황을_조회한다(String userName, String cellName) {
        world.response = thanksApi.getCellStats(thanks.tokenOf(userName), thanks.cellIdOf(cellName));
    }

    @만일("{string}가 없는 구역의 현황을 조회한다")
    public void 없는_구역의_현황을_조회한다(String userName) {
        world.response = thanksApi.getCellStats(thanks.tokenOf(userName), UNKNOWN_CELL_ID);
    }

    @만일("로그인하지 않고 구역 {string}의 현황을 조회한다")
    public void 로그인하지_않고_구역_현황을_조회한다(String cellName) {
        world.response = thanksApi.getCellStatsWithoutAuth(thanks.cellIdOf(cellName));
    }

    // --- 확인 ---

    @그러면("응답한 현황의 구역은 {string}이다")
    public void 응답한_현황의_구역은_이다(String cellName) {
        assertThat(world.response.jsonPath().getLong("cellId")).isEqualTo(thanks.cellIdOf(cellName));
    }

    // 구역원 순서는 상관없다(FE가 정렬한다)
    @그러면("응답한 현황의 구역원은 {string}이다")
    public void 응답한_현황의_구역원은_이다(String userNames) {
        List<Long> userIds = Arrays.stream(userNames.split(", ")).map(thanks::userIdOf).toList();
        assertThat(world.response.jsonPath().getList("members.user.userId", Long.class))
                .containsExactlyInAnyOrderElementsOf(userIds);
    }

    @그러면("응답한 현황의 구역원은 없다")
    public void 응답한_현황의_구역원은_없다() {
        assertThat(world.response.jsonPath().getList("members")).isEmpty();
    }

    @그러면("응답한 현황에서 {string}는 올린 감사가 없다")
    public void 응답한_현황에서_올린_감사가_없다(String userName) {
        Map<String, Object> member = memberOf(userName);
        assertThat(member).containsEntry("lastPostedAt", null);
        assertThat(member).containsEntry("dailyCounts", List.of());
    }

    // "2026-11-08=2, 2026-11-09=1"처럼 쓴다. 날짜 순서는 상관없다(FE가 정렬한다)
    @그러면("응답한 현황에서 {string}의 날짜별 개수는 {string}이다")
    public void 응답한_현황의_날짜별_개수(String userName, String dailyCounts) {
        Map<String, Integer> expected = Arrays.stream(dailyCounts.split(", "))
                .map(dailyCount -> dailyCount.split("="))
                .collect(Collectors.toMap(pair -> pair[0], pair -> Integer.valueOf(pair[1])));
        Map<String, Integer> actual = dailyCountsOf(userName).stream()
                .collect(Collectors.toMap(
                        dailyCount -> (String) dailyCount.get("date"),
                        dailyCount -> (Integer) dailyCount.get("count")));
        assertThat(actual).isEqualTo(expected);
        assertThat(dailyCountsOf(userName)).hasSameSizeAs(expected.keySet());
    }

    // 올릴 때 응답의 createdAt은 메모리 값이라 Linux에서 나노초까지 나오므로, DB에 저장된 값과 비교한다
    @그러면("응답한 현황에서 {string}의 마지막 올린 시각은 감사 {string}의 올린 시각이다")
    public void 응답한_현황의_마지막_올린_시각(String userName, String content) {
        LocalDateTime createdAt = jdbcTemplate.queryForObject(
                "SELECT created_at FROM thanks_entry WHERE id = ?", LocalDateTime.class, thanks.entryIdOf(content));
        assertThat(LocalDateTime.parse((String) memberOf(userName).get("lastPostedAt"))).isEqualTo(createdAt);
    }

    @그러면("응답한 현황에서 {string}의 이름이 나오고 프로필 사진은 {string}이다")
    public void 응답한_현황의_이름과_프로필_사진(String userName, String profileImageUrl) {
        Map<String, Object> user = userOf(userName);
        assertThat(user.get("name")).isEqualTo(thanks.currentNameOf(userName));
        assertThat(user.get("profileImageUrl")).isEqualTo(profileImageUrl);
    }

    @그러면("응답한 현황에서 {string}의 이름이 나오고 프로필 사진은 없다")
    public void 응답한_현황의_이름과_프로필_사진_없음(String userName) {
        Map<String, Object> user = userOf(userName);
        assertThat(user.get("name")).isEqualTo(thanks.currentNameOf(userName));
        assertThat(user).containsEntry("profileImageUrl", null);
    }

    @그러면("응답한 현황의 키는 정확히 {string}이고 구역원 키는 정확히 {string}이다")
    public void 응답한_현황의_키(String responseKeys, String memberKeys) {
        Map<String, Object> body = world.response.jsonPath().getMap("$");
        assertThat(body.keySet()).containsExactlyInAnyOrder(responseKeys.split(", "));
        Map<String, Object> member = world.response.jsonPath().getMap("members[0]");
        assertThat(member.keySet()).containsExactlyInAnyOrder(memberKeys.split(", "));
    }

    @그러면("응답한 현황의 사용자 키는 정확히 {string}이고 날짜별 개수 키는 정확히 {string}이다")
    public void 응답한_현황의_사용자_키(String userKeys, String dailyCountKeys) {
        Map<String, Object> user = world.response.jsonPath().getMap("members[0].user");
        assertThat(user.keySet()).containsExactlyInAnyOrder(userKeys.split(", "));
        Map<String, Object> dailyCount = world.response.jsonPath().getMap("members[0].dailyCounts[0]");
        assertThat(dailyCount.keySet()).containsExactlyInAnyOrder(dailyCountKeys.split(", "));
    }

    private Map<String, Object> memberOf(String userName) {
        return world.response.jsonPath().getMap("members.find { it.user.userId == " + thanks.userIdOf(userName) + " }");
    }

    private Map<String, Object> userOf(String userName) {
        return world.response.jsonPath().getMap("members.find { it.user.userId == " + thanks.userIdOf(userName) + " }.user");
    }

    private List<Map<String, Object>> dailyCountsOf(String userName) {
        return world.response.jsonPath().getList("members.find { it.user.userId == " + thanks.userIdOf(userName) + " }.dailyCounts");
    }
}
