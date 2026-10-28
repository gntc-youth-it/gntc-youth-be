package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ThanksApi;
import com.gntcyouthbe.acceptance.support.context.ThanksScenarioContext;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class ThanksReactionStepDefs {

    private static final Long UNKNOWN_ENTRY_ID = 999_999L;

    private final World world;
    private final ThanksScenarioContext thanks;
    private final ThanksApi thanksApi;
    private final JdbcTemplate jdbcTemplate;

    public ThanksReactionStepDefs(World world, ThanksScenarioContext thanks, ThanksApi thanksApi,
            JdbcTemplate jdbcTemplate) {
        this.world = world;
        this.thanks = thanks;
        this.thanksApi = thanksApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 응답의 반응 하나. 순서는 FE가 정하므로 비교할 때 보지 않는다.
    private record Reaction(String emoji, long count, boolean reactedByMe) {
    }

    // --- 요청 ---

    @먼저("{string}가 감사 {string}에 {word} 반응을 눌렀다")
    public void 반응을_눌렀다(String userName, String content, String emoji) {
        반응을_누른다(userName, content, emoji);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("{string}가 감사 {string}에 {word} 반응을 누른다")
    public void 반응을_누른다(String userName, String content, String emoji) {
        world.response = thanksApi.addReaction(thanks.tokenOf(userName), thanks.entryIdOf(content), emoji);
    }

    @만일("{string}가 감사 {string}의 {word} 반응을 끈다")
    public void 반응을_끈다(String userName, String content, String emoji) {
        world.response = thanksApi.removeReaction(thanks.tokenOf(userName), thanks.entryIdOf(content), emoji);
    }

    @만일("{string}가 없는 감사에 {word} 반응을 누른다")
    public void 없는_감사에_반응을_누른다(String userName, String emoji) {
        world.response = thanksApi.addReaction(thanks.tokenOf(userName), UNKNOWN_ENTRY_ID, emoji);
    }

    @만일("{string}가 없는 감사의 {word} 반응을 끈다")
    public void 없는_감사의_반응을_끈다(String userName, String emoji) {
        world.response = thanksApi.removeReaction(thanks.tokenOf(userName), UNKNOWN_ENTRY_ID, emoji);
    }

    @만일("로그인하지 않고 감사 {string}에 {word} 반응을 누른다")
    public void 로그인하지_않고_반응을_누른다(String content, String emoji) {
        world.response = thanksApi.addReactionWithoutAuth(thanks.entryIdOf(content), emoji);
    }

    @만일("로그인하지 않고 감사 {string}의 {word} 반응을 끈다")
    public void 로그인하지_않고_반응을_끈다(String content, String emoji) {
        world.response = thanksApi.removeReactionWithoutAuth(thanks.entryIdOf(content), emoji);
    }

    // --- 반응 요약 응답 확인 ---

    @그러면("응답한 반응 요약은 감사 {string}의 것이다")
    public void 응답한_반응_요약의_감사(String content) {
        assertThat(world.response.jsonPath().getLong("entryId")).isEqualTo(thanks.entryIdOf(content));
    }

    @그러면("응답한 반응은 다음과 같다")
    public void 응답한_반응은_다음과_같다(DataTable expected) {
        assertThat(reactionsOf(world.response.jsonPath().getList("reactions")))
                .containsExactlyInAnyOrderElementsOf(expectedReactions(expected));
    }

    @그러면("응답한 반응은 비어 있다")
    public void 응답한_반응은_비어_있다() {
        assertThat(world.response.jsonPath().getList("reactions")).isEmpty();
    }

    @그러면("응답한 반응 요약의 키는 정확히 {string}이고 반응 키는 정확히 {string}이다")
    public void 응답한_반응_요약의_키(String summaryKeys, String reactionKeys) {
        assertThat(world.response.jsonPath().getMap("$")).containsOnlyKeys(splitByComma(summaryKeys));
        List<Map<String, Object>> reactions = world.response.jsonPath().getList("reactions");
        assertThat(reactions).isNotEmpty();
        assertThat(reactions).allSatisfy(reaction -> assertThat(reaction).containsOnlyKeys(splitByComma(reactionKeys)));
    }

    // --- 감사 목록 확인 ---

    @그러면("감사 목록에서 감사 {string}의 반응은 다음과 같다")
    public void 감사_목록의_반응은_다음과_같다(String content, DataTable expected) {
        assertThat(reactionsOf(listedReactionsOf(content))).containsExactlyInAnyOrderElementsOf(expectedReactions(expected));
    }

    @그러면("감사 목록에서 감사 {string}의 반응은 비어 있다")
    public void 감사_목록의_반응은_비어_있다(String content) {
        assertThat(listedReactionsOf(content)).isEmpty();
    }

    @그러면("감사 {string}의 반응이 DB에 남아 있지 않다")
    public void 감사의_반응이_DB에_남아_있지_않다(String content) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM thanks_reaction WHERE entry_id = ?", Long.class, thanks.entryIdOf(content));
        assertThat(count).isZero();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listedReactionsOf(String content) {
        Long entryId = thanks.entryIdOf(content);
        List<Map<String, Object>> entries = world.response.jsonPath().getList("entries");
        return entries.stream()
                .filter(entry -> ((Number) entry.get("id")).longValue() == entryId)
                .map(entry -> (List<Map<String, Object>>) entry.get("reactions"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("감사 목록에 감사 \"" + content + "\"이 없습니다."));
    }

    private List<Reaction> reactionsOf(List<Map<String, Object>> reactions) {
        return reactions.stream()
                .map(reaction -> new Reaction(
                        (String) reaction.get("emoji"),
                        ((Number) reaction.get("count")).longValue(),
                        (Boolean) reaction.get("reactedByMe")))
                .toList();
    }

    // | 반응 | 개수 | 내가 누름 | 표를 읽는다. 내가 누름은 "예" 또는 "아니오"다.
    private List<Reaction> expectedReactions(DataTable table) {
        return table.asMaps().stream()
                .map(row -> new Reaction(
                        row.get("반응"),
                        Long.parseLong(row.get("개수")),
                        "예".equals(row.get("내가 누름"))))
                .toList();
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .toList();
    }
}
