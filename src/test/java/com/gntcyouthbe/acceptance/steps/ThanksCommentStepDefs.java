package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ThanksApi;
import com.gntcyouthbe.acceptance.support.context.ThanksScenarioContext;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class ThanksCommentStepDefs {

    private static final Long UNKNOWN_ENTRY_ID = 999_999L;
    private static final Long UNKNOWN_COMMENT_ID = 999_999L;

    private final World world;
    private final ThanksScenarioContext thanks;
    private final ThanksApi thanksApi;
    private final JdbcTemplate jdbcTemplate;

    public ThanksCommentStepDefs(World world, ThanksScenarioContext thanks, ThanksApi thanksApi,
            JdbcTemplate jdbcTemplate) {
        this.world = world;
        this.thanks = thanks;
        this.thanksApi = thanksApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- 요청 ---

    @먼저("{string}가 감사 {string}에 댓글 {string}을/를 달았다")
    public void 댓글을_달았다(String userName, String content, String comment) {
        댓글을_단다(userName, content, comment);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @만일("{string}가 감사 {string}에 댓글 {string}을/를 단다")
    public void 댓글을_단다(String userName, String content, String comment) {
        post(userName, thanks.entryIdOf(content), Map.of("content", comment));
    }

    @만일("{string}가 감사 {string}에 {int}자짜리 댓글을 단다")
    public void 글자_수를_정해_댓글을_단다(String userName, String content, int length) {
        댓글을_단다(userName, content, "댓".repeat(length));
    }

    @만일("{string}가 감사 {string}에 앞뒤에 공백을 두른 {int}자짜리 댓글을 단다")
    public void 앞뒤에_공백을_두른_댓글을_단다(String userName, String content, int length) {
        댓글을_단다(userName, content, "  " + "댓".repeat(length) + "  ");
    }

    @만일("{string}가 감사 {string}에 내용 없이 댓글을 단다")
    public void 내용_없이_댓글을_단다(String userName, String content) {
        post(userName, thanks.entryIdOf(content), Map.of());
    }

    @만일("{string}가 없는 감사에 댓글 {string}을/를 단다")
    public void 없는_감사에_댓글을_단다(String userName, String comment) {
        post(userName, UNKNOWN_ENTRY_ID, Map.of("content", comment));
    }

    @만일("{string}가 감사 {string}의 댓글을 조회한다")
    public void 댓글을_조회한다(String userName, String content) {
        world.response = thanksApi.getComments(thanks.tokenOf(userName), thanks.entryIdOf(content));
    }

    @만일("{string}가 없는 감사의 댓글을 조회한다")
    public void 없는_감사의_댓글을_조회한다(String userName) {
        world.response = thanksApi.getComments(thanks.tokenOf(userName), UNKNOWN_ENTRY_ID);
    }

    @만일("{string}가 댓글 {string}을/를 지운다")
    public void 댓글을_지운다(String userName, String comment) {
        world.response = thanksApi.deleteComment(thanks.tokenOf(userName), thanks.commentIdOf(comment));
    }

    @먼저("{string}가 댓글 {string}을/를 지웠다")
    public void 댓글을_지웠다(String userName, String comment) {
        댓글을_지운다(userName, comment);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("{string}가 없는 댓글을 지운다")
    public void 없는_댓글을_지운다(String userName) {
        world.response = thanksApi.deleteComment(thanks.tokenOf(userName), UNKNOWN_COMMENT_ID);
    }

    @만일("로그인하지 않고 감사 {string}의 댓글을 조회한다")
    public void 로그인하지_않고_댓글을_조회한다(String content) {
        world.response = thanksApi.getCommentsWithoutAuth(thanks.entryIdOf(content));
    }

    @만일("로그인하지 않고 감사 {string}에 댓글 {string}을/를 단다")
    public void 로그인하지_않고_댓글을_단다(String content, String comment) {
        world.response = thanksApi.addCommentWithoutAuth(thanks.entryIdOf(content), Map.of("content", comment));
    }

    @만일("로그인하지 않고 댓글 {string}을/를 지운다")
    public void 로그인하지_않고_댓글을_지운다(String comment) {
        world.response = thanksApi.deleteCommentWithoutAuth(thanks.commentIdOf(comment));
    }

    // --- 응답한 댓글 목록 확인 ---

    @그러면("응답한 댓글 목록은 감사 {string}의 것이다")
    public void 응답한_댓글_목록의_감사(String content) {
        assertThat(world.response.jsonPath().getLong("entryId")).isEqualTo(thanks.entryIdOf(content));
    }

    @그러면("응답한 댓글은 차례대로 {string}이다")
    public void 응답한_댓글은_차례대로(String comments) {
        assertThat(world.response.jsonPath().getList("comments.content", String.class))
                .containsExactlyElementsOf(splitByComma(comments));
    }

    @그러면("응답한 댓글 목록은 비어 있다")
    public void 응답한_댓글_목록은_비어_있다() {
        assertThat(comments()).isEmpty();
    }

    @그러면("응답한 마지막 댓글은 {int}자이다")
    public void 응답한_마지막_댓글의_길이(int length) {
        assertThat((String) comments().getLast().get("content")).hasSize(length);
    }

    @그러면("응답한 댓글 목록의 키는 정확히 {string}이고 댓글 키는 정확히 {string}이다")
    public void 응답한_댓글_목록의_키(String listKeys, String commentKeys) {
        assertThat(world.response.jsonPath().getMap("$")).containsOnlyKeys(splitByComma(listKeys));
        assertThat(comments()).isNotEmpty();
        assertThat(comments()).allSatisfy(comment -> assertThat(comment).containsOnlyKeys(splitByComma(commentKeys)));
    }

    @그러면("응답한 댓글 {string}의 작성자는 {string}이고 프로필 사진은 {string}이다")
    public void 응답한_댓글의_작성자와_프로필_사진(String comment, String userName, String profileImageUrl) {
        Map<String, Object> author = authorOf(commentOf(comment));
        assertAuthor(author, userName);
        assertThat(author.get("profileImageUrl")).isEqualTo(profileImageUrl);
    }

    @그러면("응답한 댓글 {string}의 작성자는 {string}이고 프로필 사진은 없다")
    public void 응답한_댓글의_작성자와_프로필_사진_없음(String comment, String userName) {
        Map<String, Object> author = authorOf(commentOf(comment));
        assertAuthor(author, userName);
        assertThat(author).containsEntry("profileImageUrl", null);
    }

    @그러면("응답한 댓글 {string}은/는 내 댓글로 표시된다")
    public void 내_댓글로_표시된다(String comment) {
        assertThat(commentOf(comment).get("isMine")).isEqualTo(true);
    }

    @그러면("응답한 댓글 {string}은/는 내 댓글이 아닌 것으로 표시된다")
    public void 내_댓글이_아닌_것으로_표시된다(String comment) {
        assertThat(commentOf(comment).get("isMine")).isEqualTo(false);
    }

    // --- 감사 목록 확인 ---

    @그러면("감사 목록에서 감사 {string}의 댓글은 {int}개이고, 최근 댓글은 {string}가 쓴 {string}이다")
    public void 감사_목록의_댓글_수와_최근_댓글(String content, int count, String userName, String comment) {
        Map<String, Object> entry = listedEntryOf(content);
        assertThat(((Number) entry.get("commentCount")).intValue()).isEqualTo(count);
        Map<String, Object> latestComment = latestCommentOf(entry);
        assertAuthor(authorOf(latestComment), userName);
        assertThat(latestComment.get("content")).isEqualTo(comment);
    }

    // 감사 하나를 돌려주는 응답(감사제목 고치기 등)의 댓글 수와 최근 댓글
    @그러면("응답한 감사의 댓글은 {int}개이고, 최근 댓글은 {string}가 쓴 {string}이다")
    public void 응답한_감사의_댓글_수와_최근_댓글(int count, String userName, String comment) {
        Map<String, Object> entry = world.response.jsonPath().getMap("$");
        assertThat(((Number) entry.get("commentCount")).intValue()).isEqualTo(count);
        Map<String, Object> latestComment = latestCommentOf(entry);
        assertAuthor(authorOf(latestComment), userName);
        assertThat(latestComment.get("content")).isEqualTo(comment);
    }

    @그러면("감사 목록에서 감사 {string}의 최근 댓글 작성자 프로필 사진은 {string}이다")
    public void 감사_목록의_최근_댓글_작성자_프로필_사진(String content, String profileImageUrl) {
        assertThat(authorOf(latestCommentOf(listedEntryOf(content))).get("profileImageUrl")).isEqualTo(profileImageUrl);
    }

    @그러면("감사 목록에서 감사 {string}의 최근 댓글 키는 정확히 {string}이다")
    public void 감사_목록의_최근_댓글_키(String content, String keys) {
        assertThat(latestCommentOf(listedEntryOf(content))).containsOnlyKeys(splitByComma(keys));
    }

    @그러면("감사 목록에서 감사 {string}의 댓글은 없다")
    public void 감사_목록의_댓글은_없다(String content) {
        Map<String, Object> entry = listedEntryOf(content);
        assertThat(((Number) entry.get("commentCount")).intValue()).isZero();
        assertThat(entry).containsEntry("latestComment", null);
    }

    @그러면("감사 {string}의 댓글이 DB에 남아 있지 않다")
    public void 감사의_댓글이_DB에_남아_있지_않다(String content) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM thanks_comment WHERE entry_id = ?", Long.class, thanks.entryIdOf(content));
        assertThat(count).isZero();
    }

    // 응답은 그 감사의 댓글 전체라서, 방금 단 댓글은 같은 내용 중 id가 가장 큰 것이다
    private void post(String userName, Long entryId, Map<String, Object> body) {
        world.response = thanksApi.addComment(thanks.tokenOf(userName), entryId, body);
        if (world.response.statusCode() == HttpStatus.CREATED.value()) {
            Map<String, Object> created = comments().stream()
                    .max(Comparator.comparingLong(comment -> ((Number) comment.get("id")).longValue()))
                    .orElseThrow();
            thanks.saveCommentId((String) created.get("content"), ((Number) created.get("id")).longValue());
        }
    }

    private List<Map<String, Object>> comments() {
        return world.response.jsonPath().getList("comments");
    }

    private Map<String, Object> commentOf(String comment) {
        Long commentId = thanks.commentIdOf(comment);
        return comments().stream()
                .filter(item -> ((Number) item.get("id")).longValue() == commentId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("응답에 댓글 \"" + comment + "\"이 없습니다."));
    }

    private Map<String, Object> listedEntryOf(String content) {
        Long entryId = thanks.entryIdOf(content);
        List<Map<String, Object>> entries = world.response.jsonPath().getList("entries");
        return entries.stream()
                .filter(entry -> ((Number) entry.get("id")).longValue() == entryId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("감사 목록에 감사 \"" + content + "\"이 없습니다."));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> latestCommentOf(Map<String, Object> entry) {
        Map<String, Object> latestComment = (Map<String, Object>) entry.get("latestComment");
        assertThat(latestComment).isNotNull();
        return latestComment;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> authorOf(Map<String, Object> item) {
        return (Map<String, Object>) item.get("author");
    }

    private void assertAuthor(Map<String, Object> author, String userName) {
        assertThat(((Number) author.get("userId")).longValue()).isEqualTo(thanks.userIdOf(userName));
        assertThat(author.get("name")).isEqualTo(thanks.currentNameOf(userName));
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .toList();
    }
}
