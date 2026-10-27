package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.UserProfileApi;
import com.gntcyouthbe.acceptance.support.context.ThanksScenarioContext;
import com.gntcyouthbe.acceptance.support.context.World;
import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class ThanksProfileStepDefs {

    private static final long NEWCOMER_ID = 701L;
    private static final Long UNKNOWN_FILE_ID = 999_999L;

    private final World world;
    private final ThanksScenarioContext thanks;
    private final UserProfileApi userProfileApi;
    private final AuthApi authApi;
    private final JwtService jwtService;
    private final UploadedFileRepository uploadedFileRepository;
    private final JdbcTemplate jdbcTemplate;

    private String refreshedAccessToken;

    public ThanksProfileStepDefs(World world, ThanksScenarioContext thanks, UserProfileApi userProfileApi,
            AuthApi authApi, JwtService jwtService, UploadedFileRepository uploadedFileRepository,
            JdbcTemplate jdbcTemplate) {
        this.world = world;
        this.thanks = thanks;
        this.userProfileApi = userProfileApi;
        this.authApi = authApi;
        this.jwtService = jwtService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 프로필 행이 없는 사람으로 시작해야 하므로 시나리오마다 새내기유저를 새로 만들고, 끝나면 그 사람이 남긴 것과 함께 지운다.
    // data.sql에 넣으면 사용자 수를 세는 관리자 기능 테스트가 깨진다.
    @Before("@thanks-profile")
    public void 새내기유저를_만든다() {
        jdbcTemplate.update("""
                INSERT INTO app_user (id, email, name, provider, provider_user_id, role, created_at, updated_at)
                VALUES (?, 'newcomer@example.com', '새내기유저', 'KAKAO', 'kakao_newcomer', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, NEWCOMER_ID);
    }

    @After("@thanks-profile")
    public void 새내기유저를_지운다() {
        jdbcTemplate.update("DELETE FROM thanks_entry WHERE user_id = ?", NEWCOMER_ID);
        jdbcTemplate.update("DELETE FROM user_profile WHERE user_id = ?", NEWCOMER_ID);
        jdbcTemplate.update("DELETE FROM app_user WHERE id = ?", NEWCOMER_ID);
    }

    // --- 이름·사진 저장 (PATCH) ---

    @만일("{string}가 프로필 사진을 {string}(으)로 바꾼다")
    public void 프로필_사진을_바꾼다(String userName, String fileName) {
        patch(userName, Map.of("profileImageId", thanks.fileIdOf(fileName)));
    }

    @먼저("{string}가 프로필 사진을 {string}(으)로 바꿨다")
    public void 프로필_사진을_바꿨다(String userName, String fileName) {
        프로필_사진을_바꾼다(userName, fileName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("{string}가 프로필 이름을 {string}(으)로 바꾼다")
    public void 프로필_이름을_바꾼다(String userName, String name) {
        patch(userName, Map.of("name", name));
    }

    @만일("{string}가 프로필 이름을 {string}, 사진을 {string}(으)로 바꾼다")
    public void 프로필_이름과_사진을_바꾼다(String userName, String name, String fileName) {
        patch(userName, Map.of("name", name, "profileImageId", thanks.fileIdOf(fileName)));
    }

    @먼저("{string}가 프로필 이름을 {string}, 사진을 {string}(으)로 바꿨다")
    public void 프로필_이름과_사진을_바꿨다(String userName, String name, String fileName) {
        프로필_이름과_사진을_바꾼다(userName, name, fileName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("{string}가 프로필 이름을 {int}자로 바꾼다")
    public void 프로필_이름을_글자_수를_정해_바꾼다(String userName, int length) {
        프로필_이름을_바꾼다(userName, "가".repeat(length));
    }

    @만일("{string}가 프로필 이름을 앞뒤에 공백을 두른 {int}자로 바꾼다")
    public void 프로필_이름을_앞뒤에_공백을_둘러_바꾼다(String userName, int length) {
        프로필_이름을_바꾼다(userName, "  " + "가".repeat(length) + "  ");
    }

    @만일("{string}가 아무것도 보내지 않고 프로필을 바꾼다")
    public void 아무것도_보내지_않고_프로필을_바꾼다(String userName) {
        patch(userName, Map.of());
    }

    @만일("{string}가 없는 파일로 프로필 사진을 바꾼다")
    public void 없는_파일로_프로필_사진을_바꾼다(String userName) {
        patch(userName, Map.of("profileImageId", UNKNOWN_FILE_ID));
    }

    @만일("{string}가 지금 쓰는 프로필 사진을 다시 보낸다")
    public void 지금_쓰는_프로필_사진을_다시_보낸다(String userName) {
        Long currentImageId = userProfileApi.getProfile(thanks.tokenOf(userName)).jsonPath().getLong("profileImageId");
        patch(userName, Map.of("profileImageId", currentImageId));
    }

    @만일("로그인하지 않고 프로필 이름을 {string}(으)로 바꾼다")
    public void 로그인하지_않고_프로필_이름을_바꾼다(String name) {
        world.response = userProfileApi.updateProfileWithoutAuth(Map.of("name", name));
    }

    // --- 기존 API ---

    @만일("{string}가 내 프로필을 조회한다")
    public void 내_프로필을_조회한다(String userName) {
        world.response = userProfileApi.getProfile(thanks.tokenOf(userName));
    }

    @만일("{string}가 이름 {string}, 성전 {word}, 기수 {int}로 프로필 전체를 저장한다")
    public void 프로필_전체를_저장한다(String userName, String name, String churchId, int generation) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("churchId", churchId);
        body.put("generation", generation);
        body.put("phoneNumber", "010-1234-5678");
        body.put("gender", "MALE");
        world.response = userProfileApi.saveProfile(thanks.tokenOf(userName), body);
    }

    @먼저("{string}가 이름 {string}, 성전 {word}, 기수 {int}로 프로필 전체를 저장했다")
    public void 프로필_전체를_저장했다(String userName, String name, String churchId, int generation) {
        프로필_전체를_저장한다(userName, name, churchId, generation);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("{string}가 이름만 보내서 프로필 전체를 저장한다")
    public void 이름만_보내서_프로필_전체를_저장한다(String userName) {
        world.response = userProfileApi.saveProfile(thanks.tokenOf(userName), Map.of("name", "새내기"));
    }

    // FE는 저장한 뒤 refresh로 토큰을 새로 받아 헤더와 감사로그의 내 이름·사진을 바꾼다
    @만일("{string}가 토큰을 새로 받는다")
    public void 토큰을_새로_받는다(String userName) {
        world.response = authApi.refresh(thanks.refreshTokenOf(userName));
        if (world.response.statusCode() == HttpStatus.OK.value()) {
            refreshedAccessToken = world.response.jsonPath().getString("accessToken");
        }
    }

    // --- 응답한 프로필 확인 ---

    @그러면("응답한 프로필의 이름은 {string}이다")
    public void 응답한_프로필의_이름(String name) {
        assertThat(world.response.jsonPath().getString("name")).isEqualTo(name);
    }

    @그러면("응답한 프로필의 이름은 {int}자이다")
    public void 응답한_프로필의_이름_길이(int length) {
        assertThat(world.response.jsonPath().getString("name")).hasSize(length);
    }

    @그러면("응답한 프로필의 사진은 {string}이다")
    public void 응답한_프로필의_사진(String fileName) {
        assertThat(world.response.jsonPath().getLong("profileImageId")).isEqualTo(thanks.fileIdOf(fileName));
        assertThat(world.response.jsonPath().getString("profileImagePath")).isEqualTo(filePathOf(fileName));
    }

    @그러면("응답한 프로필의 사진은 없다")
    public void 응답한_프로필의_사진은_없다() {
        Map<String, Object> profile = world.response.jsonPath().getMap("$");
        assertThat(profile).containsEntry("profileImageId", null);
        assertThat(profile).containsEntry("profileImagePath", null);
    }

    @그러면("응답한 프로필의 사진 경로는 {string}이다")
    public void 응답한_프로필의_사진_경로(String profileImagePath) {
        assertThat(world.response.jsonPath().getString("profileImagePath")).isEqualTo(profileImagePath);
    }

    @그러면("응답한 프로필의 기수, 전화번호, 성별은 비어 있다")
    public void 응답한_프로필의_기수_전화번호_성별은_비어_있다() {
        Map<String, Object> profile = world.response.jsonPath().getMap("$");
        for (String key : List.of("generation", "phoneNumber", "gender", "genderDisplay")) {
            assertThat(profile).containsEntry(key, null);
        }
    }

    @그러면("응답한 프로필의 기수는 {int}, 전화번호는 {string}, 성별은 {word}이다")
    public void 응답한_프로필의_기수_전화번호_성별(int generation, String phoneNumber, String gender) {
        assertThat(world.response.jsonPath().getInt("generation")).isEqualTo(generation);
        assertThat(world.response.jsonPath().getString("phoneNumber")).isEqualTo(phoneNumber);
        assertThat(world.response.jsonPath().getString("gender")).isEqualTo(gender);
    }

    // --- 다른 곳에 반영됐는지 확인 ---

    @그러면("새로 받은 토큰의 이름은 {string}이고 프로필 사진은 {string}이다")
    public void 새로_받은_토큰의_이름과_프로필_사진(String name, String fileName) {
        Map<String, Object> claims = jwtService.getClaims(refreshedAccessToken);
        assertThat(claims.get("name")).isEqualTo(name);
        assertThat(claims.get("profileImagePath")).isEqualTo(filePathOf(fileName));
    }

    @그러면("새로 받은 토큰의 프로필 사진은 {string}이다")
    public void 새로_받은_토큰의_프로필_사진(String fileName) {
        assertThat(jwtService.getClaims(refreshedAccessToken).get("profileImagePath")).isEqualTo(filePathOf(fileName));
    }

    // 이름이 바뀌었으므로 로그인할 때의 이름이 아니라 적은 이름과 비교한다
    @그러면("감사 목록에서 감사 {string}의 작성자 이름은 {string}이고 프로필 사진은 {string}이다")
    @SuppressWarnings("unchecked")
    public void 감사_목록의_작성자_이름과_프로필_사진(String content, String name, String fileName) {
        Long entryId = thanks.entryIdOf(content);
        List<Map<String, Object>> entries = world.response.jsonPath().getList("entries");
        Map<String, Object> author = entries.stream()
                .filter(entry -> ((Number) entry.get("id")).longValue() == entryId)
                .map(entry -> (Map<String, Object>) entry.get("author"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("감사 목록에 감사 \"" + content + "\"이 없습니다."));
        assertThat(author.get("name")).isEqualTo(name);
        assertThat(author.get("profileImageUrl")).isEqualTo(filePathOf(fileName));
    }

    @그러면("파일 {string}의 업로드 기록이 남아 있다")
    public void 파일의_업로드_기록이_남아_있다(String fileName) {
        assertThat(uploadedFileRepository.existsById(thanks.fileIdOf(fileName))).isTrue();
    }

    private void patch(String userName, Map<String, Object> body) {
        world.response = userProfileApi.updateProfile(thanks.tokenOf(userName), body);
    }

    private String filePathOf(String fileName) {
        return uploadedFileRepository.findById(thanks.fileIdOf(fileName)).orElseThrow().getFilePath();
    }
}
