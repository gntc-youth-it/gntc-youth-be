package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.FileApi;
import com.gntcyouthbe.acceptance.support.api.ThanksApi;
import com.gntcyouthbe.acceptance.support.context.ThanksScenarioContext;
import com.gntcyouthbe.acceptance.support.context.World;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ThanksStepDefs {

    private static final Long UNKNOWN_FILE_ID = 999_999L;
    private static final Long UNKNOWN_ENTRY_ID = 999_999L;

    private final World world;
    private final ThanksScenarioContext thanks;
    private final ThanksApi thanksApi;
    private final FileApi fileApi;
    private final ThanksEntryRepository thanksEntryRepository;
    private final UploadedFileRepository uploadedFileRepository;

    public ThanksStepDefs(World world, ThanksScenarioContext thanks, ThanksApi thanksApi, FileApi fileApi,
            ThanksEntryRepository thanksEntryRepository, UploadedFileRepository uploadedFileRepository) {
        this.world = world;
        this.thanks = thanks;
        this.thanksApi = thanksApi;
        this.fileApi = fileApi;
        this.thanksEntryRepository = thanksEntryRepository;
        this.uploadedFileRepository = uploadedFileRepository;
    }

    // 시나리오끼리 DB를 같이 쓰므로, 날짜·칸마다 하나만 올릴 수 있는 제약에 걸리지 않게 매번 비운다
    @Before("@thanks")
    public void 감사_기록을_비운다() {
        thanksEntryRepository.deleteAllInBatch();
    }

    // --- 준비 ---

    // 업로드 기록(uploaded_file)은 presigned URL을 받을 때 요청한 사용자 이름으로 만들어진다
    @먼저("{string}가 사진 파일 {string}을/를 업로드했다")
    public void 사진_파일을_업로드했다(String userName, String fileName) {
        upload(userName, fileName, "thanks.webp", "image/webp");
    }

    @먼저("{string}가 영상 파일 {string}을/를 업로드했다")
    public void 영상_파일을_업로드했다(String userName, String fileName) {
        upload(userName, fileName, "thanks.mp4", "video/mp4");
    }

    @먼저("{string}가 문서 파일 {string}을/를 업로드했다")
    public void 문서_파일을_업로드했다(String userName, String fileName) {
        upload(userName, fileName, "bulletin.pdf", "application/pdf");
    }

    @먼저("{string}가 {string}을/를 붙여 감사 {string}을/를 올렸다")
    public void 파일을_붙여_감사를_올렸다(String userName, String fileName, String content) {
        파일을_붙여_감사를_올린다(userName, fileName, content);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    // 어떤 파일인지 중요하지 않을 때는 사진을 새로 업로드해서 붙인다
    @먼저("{string}가 감사 {string}을/를 올렸다")
    public void 감사를_올렸다(String userName, String content) {
        String fileName = content + " 사진";
        사진_파일을_업로드했다(userName, fileName);
        파일을_붙여_감사를_올렸다(userName, fileName, content);
    }

    @먼저("{string}가 감사 {string}을/를 지웠다")
    public void 감사를_지웠다(String userName, String content) {
        감사를_지운다(userName, content);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    // --- 요청 ---

    @만일("{string}가 {string}을/를 붙여 감사 {string}을/를 올린다")
    public void 파일을_붙여_감사를_올린다(String userName, String fileName, String content) {
        post(userName, Map.of("content", content, "fileId", thanks.fileIdOf(fileName)));
    }

    @만일("{string}가 {string}을/를 붙여 {int}자짜리 감사를 올린다")
    public void 글자_수를_정해_감사를_올린다(String userName, String fileName, int length) {
        파일을_붙여_감사를_올린다(userName, fileName, "감".repeat(length));
    }

    @만일("{string}가 {string}을/를 붙여 앞뒤에 공백을 두른 {int}자짜리 감사를 올린다")
    public void 앞뒤에_공백을_두른_감사를_올린다(String userName, String fileName, int length) {
        파일을_붙여_감사를_올린다(userName, fileName, "  " + "감".repeat(length) + "  ");
    }

    @만일("{string}가 {string}을/를 붙여 감사제목 없이 올린다")
    public void 감사제목_없이_올린다(String userName, String fileName) {
        post(userName, Map.of("fileId", thanks.fileIdOf(fileName)));
    }

    @만일("{string}가 파일 없이 감사 {string}을/를 올린다")
    public void 파일_없이_감사를_올린다(String userName, String content) {
        post(userName, Map.of("content", content));
    }

    @만일("{string}가 없는 파일을 붙여 감사 {string}을/를 올린다")
    public void 없는_파일을_붙여_감사를_올린다(String userName, String content) {
        post(userName, Map.of("content", content, "fileId", UNKNOWN_FILE_ID));
    }

    @만일("{string}가 {string}을/를 붙여 감사 {string}을/를 올리면서 날짜 {string}와/과 칸 {word}도 보낸다")
    public void 날짜와_칸도_보내며_감사를_올린다(String userName, String fileName, String content,
            String missionDate, String slot) {
        post(userName, Map.of(
                "content", content,
                "fileId", thanks.fileIdOf(fileName),
                "missionDate", missionDate,
                "slot", slot));
    }

    @만일("{string}가 {string}의 감사 목록을 조회한다")
    public void 감사_목록을_조회한다(String userName, String date) {
        world.response = thanksApi.getEntries(thanks.tokenOf(userName), date);
    }

    @만일("{string}가 날짜 없이 감사 목록을 조회한다")
    public void 날짜_없이_감사_목록을_조회한다(String userName) {
        world.response = thanksApi.getEntriesWithoutDate(thanks.tokenOf(userName));
    }

    @만일("{string}가 감사 {string}을/를 지운다")
    public void 감사를_지운다(String userName, String content) {
        world.response = thanksApi.deleteEntry(thanks.tokenOf(userName), thanks.entryIdOf(content));
    }

    @만일("{string}가 없는 감사를 지운다")
    public void 없는_감사를_지운다(String userName) {
        world.response = thanksApi.deleteEntry(thanks.tokenOf(userName), UNKNOWN_ENTRY_ID);
    }

    @만일("로그인하지 않고 {string}의 감사 목록을 조회한다")
    public void 로그인하지_않고_감사_목록을_조회한다(String date) {
        world.response = thanksApi.getEntriesWithoutAuth(date);
    }

    // 요청 본문 검증을 통과해야 로그인 여부를 확인하므로 올바른 본문을 보낸다
    @만일("로그인하지 않고 감사를 올린다")
    public void 로그인하지_않고_감사를_올린다() {
        world.response = thanksApi.createEntryWithoutAuth(Map.of("content", "감사합니다", "fileId", UNKNOWN_FILE_ID));
    }

    @만일("로그인하지 않고 감사 {string}을/를 지운다")
    public void 로그인하지_않고_감사를_지운다(String content) {
        world.response = thanksApi.deleteEntryWithoutAuth(thanks.entryIdOf(content));
    }

    // --- 올린 감사 확인 ---

    @그러면("응답한 감사의 날짜는 {string}이고 칸은 {word}이다")
    public void 응답한_감사의_날짜와_칸(String missionDate, String slot) {
        assertThat(world.response.jsonPath().getString("missionDate")).isEqualTo(missionDate);
        assertThat(world.response.jsonPath().getString("slot")).isEqualTo(slot);
    }

    @그러면("응답한 감사의 감사제목은 {string}이다")
    public void 응답한_감사의_감사제목(String content) {
        assertThat(world.response.jsonPath().getString("content")).isEqualTo(content);
    }

    @그러면("응답한 감사의 감사제목은 {int}자이다")
    public void 응답한_감사의_감사제목_길이(int length) {
        assertThat(world.response.jsonPath().getString("content")).hasSize(length);
    }

    @그러면("응답한 감사의 파일은 {string}이고 종류는 {word}이다")
    public void 응답한_감사의_파일(String fileName, String type) {
        assertMedia(world.response.jsonPath().getMap("media"), fileName, type);
    }

    @그러면("응답한 감사의 작성자는 {string}이다")
    public void 응답한_감사의_작성자(String userName) {
        assertAuthor(world.response.jsonPath().getMap("author"), userName);
    }

    @그러면("응답한 감사는 내 감사로 표시된다")
    public void 응답한_감사는_내_감사로_표시된다() {
        assertThat(world.response.jsonPath().getBoolean("isMine")).isTrue();
    }

    // 반응(3단계)과 댓글(4단계)을 만들기 전에도 FE 타입에 맞춰 필드는 내려 준다
    @그러면("응답한 감사에는 아직 반응과 댓글이 없다")
    public void 응답한_감사에는_아직_반응과_댓글이_없다() {
        Map<String, Object> entry = world.response.jsonPath().getMap("$");
        assertThat(entry.get("reactions")).isEqualTo(List.of());
        assertThat(((Number) entry.get("commentCount")).intValue()).isZero();
        assertThat(entry).containsEntry("latestComment", null);
    }

    // 다른 API처럼 BaseEntity의 LocalDateTime을 그대로 보낸다. FE는 시간대가 없으면 UTC로 읽는다.
    @그러면("응답한 감사의 올린 시각은 시간대 없는 날짜시각이다")
    public void 응답한_감사의_올린_시각() {
        String createdAt = world.response.jsonPath().getString("createdAt");
        assertThat(createdAt).isNotNull();
        assertThat(LocalDateTime.parse(createdAt)).isNotNull();
    }

    @그러면("응답한 감사의 키는 정확히 {string}이다")
    public void 응답한_감사의_키(String keys) {
        assertThat(world.response.jsonPath().getMap("$")).containsOnlyKeys(splitByComma(keys));
    }

    @그러면("응답한 감사의 파일 키는 정확히 {string}이고 작성자 키는 정확히 {string}이다")
    public void 응답한_감사의_파일_키와_작성자_키(String mediaKeys, String authorKeys) {
        assertThat(world.response.jsonPath().getMap("media")).containsOnlyKeys(splitByComma(mediaKeys));
        assertThat(world.response.jsonPath().getMap("author")).containsOnlyKeys(splitByComma(authorKeys));
    }

    // --- 감사 목록 확인 ---

    @그러면("감사 목록의 날짜는 {string}이다")
    public void 감사_목록의_날짜(String date) {
        assertThat(world.response.jsonPath().getString("date")).isEqualTo(date);
    }

    // 순서는 FE가 사람별로 묶어 정하므로 보지 않는다
    @그러면("감사 목록의 감사는 {string}이다")
    public void 감사_목록의_감사(String contents) {
        List<Long> expected = splitByComma(contents).stream().map(thanks::entryIdOf).toList();
        assertThat(world.response.jsonPath().getList("entries.id", Long.class))
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @그러면("감사 목록은 비어 있다")
    public void 감사_목록은_비어_있다() {
        assertThat(entries()).isEmpty();
    }

    @그러면("감사 목록에서 감사 {string}은/는 내 감사로 표시된다")
    public void 목록에서_내_감사로_표시된다(String content) {
        assertThat(listedEntryOf(content).get("isMine")).isEqualTo(true);
    }

    @그러면("감사 목록에서 감사 {string}은/는 내 감사가 아닌 것으로 표시된다")
    public void 목록에서_내_감사가_아닌_것으로_표시된다(String content) {
        assertThat(listedEntryOf(content).get("isMine")).isEqualTo(false);
    }

    @그러면("감사 목록에서 감사 {string}의 작성자는 {string}이고 프로필 사진은 {string}이다")
    public void 목록에서_작성자와_프로필_사진(String content, String userName, String profileImageUrl) {
        Map<String, Object> author = authorOf(listedEntryOf(content));
        assertAuthor(author, userName);
        assertThat(author.get("profileImageUrl")).isEqualTo(profileImageUrl);
    }

    @그러면("감사 목록에서 감사 {string}의 작성자는 {string}이고 프로필 사진은 없다")
    public void 목록에서_작성자와_프로필_사진_없음(String content, String userName) {
        Map<String, Object> author = authorOf(listedEntryOf(content));
        assertAuthor(author, userName);
        assertThat(author).containsEntry("profileImageUrl", null);
    }

    @그러면("감사 목록에서 감사 {string}의 파일은 {string}이고 종류는 {word}이다")
    @SuppressWarnings("unchecked")
    public void 목록에서_파일(String content, String fileName, String type) {
        assertMedia((Map<String, Object>) listedEntryOf(content).get("media"), fileName, type);
    }

    @그러면("감사 목록 항목의 키는 정확히 {string}이다")
    public void 감사_목록_항목의_키(String keys) {
        assertThat(entries()).isNotEmpty();
        assertThat(entries()).allSatisfy(entry -> assertThat(entry).containsOnlyKeys(splitByComma(keys)));
    }

    // --- 삭제 확인 ---

    @그러면("{string}의 감사 목록에 감사 {string}이/가 있다")
    public void 감사_목록에_감사가_있다(String date, String content) {
        assertThat(listedEntryIds(date)).contains(thanks.entryIdOf(content));
    }

    @그러면("{string}의 감사 목록에 감사 {string}이/가 없다")
    public void 감사_목록에_감사가_없다(String date, String content) {
        assertThat(listedEntryIds(date)).doesNotContain(thanks.entryIdOf(content));
    }

    @그러면("파일 {string}의 업로드 기록이 지워졌다")
    public void 파일의_업로드_기록이_지워졌다(String fileName) {
        assertThat(uploadedFileRepository.existsById(thanks.fileIdOf(fileName))).isFalse();
    }

    private void upload(String userName, String fileName, String uploadFilename, String contentType) {
        ExtractableResponse<Response> response =
                fileApi.requestPresignedUrl(thanks.tokenOf(userName), uploadFilename, contentType);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        thanks.saveFileId(fileName, response.jsonPath().getLong("fileId"));
    }

    private void post(String userName, Map<String, Object> body) {
        world.response = thanksApi.createEntry(thanks.tokenOf(userName), body);
        if (world.response.statusCode() == HttpStatus.CREATED.value()) {
            thanks.saveEntryId(world.response.jsonPath().getString("content"), world.response.jsonPath().getLong("id"));
        }
    }

    // 목록은 로그인한 누구에게나 같으므로 테스트유저로 다시 불러온다
    private List<Long> listedEntryIds(String date) {
        ExtractableResponse<Response> response = thanksApi.getEntries(thanks.tokenOf("테스트유저"), date);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        return response.jsonPath().getList("entries.id", Long.class);
    }

    private void assertMedia(Map<String, Object> media, String fileName, String type) {
        assertThat(((Number) media.get("fileId")).longValue()).isEqualTo(thanks.fileIdOf(fileName));
        assertThat((String) media.get("url")).startsWith("uploads/");
        assertThat(media.get("type")).isEqualTo(type);
    }

    private void assertAuthor(Map<String, Object> author, String userName) {
        assertThat(((Number) author.get("userId")).longValue()).isEqualTo(thanks.userIdOf(userName));
        assertThat(author.get("name")).isEqualTo(thanks.currentNameOf(userName));
    }

    private List<Map<String, Object>> entries() {
        return world.response.jsonPath().getList("entries");
    }

    private Map<String, Object> listedEntryOf(String content) {
        Long entryId = thanks.entryIdOf(content);
        return entries().stream()
                .filter(entry -> ((Number) entry.get("id")).longValue() == entryId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("감사 목록에 감사 \"" + content + "\"이 없습니다."));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> authorOf(Map<String, Object> entry) {
        return (Map<String, Object>) entry.get("author");
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .toList();
    }
}
