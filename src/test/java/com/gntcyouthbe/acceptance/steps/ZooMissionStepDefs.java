package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.FileApi;
import com.gntcyouthbe.acceptance.support.api.ZooTeamApi;
import com.gntcyouthbe.acceptance.support.context.World;
import com.gntcyouthbe.acceptance.support.context.ZooScenarioContext;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class ZooMissionStepDefs {

    private static final Long UNKNOWN_FILE_ID = 999_999L;

    private final World world;
    private final ZooScenarioContext zoo;
    private final ZooTeamApi zooTeamApi;
    private final FileApi fileApi;
    private final ZooTeamMissionRepository zooTeamMissionRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Map<String, Long> photoIds = new HashMap<>();
    private String rememberedSubmittedAt;
    private String rememberedUpdatedAt;
    private String rememberedArrivedAt;

    public ZooMissionStepDefs(World world, ZooScenarioContext zoo, ZooTeamApi zooTeamApi, FileApi fileApi,
            ZooTeamMissionRepository zooTeamMissionRepository, JdbcTemplate jdbcTemplate) {
        this.world = world;
        this.zoo = zoo;
        this.zooTeamApi = zooTeamApi;
        this.fileApi = fileApi;
        this.zooTeamMissionRepository = zooTeamMissionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- 준비 ---

    // 업로드 기록(uploaded_file)은 presigned URL을 받을 때 그 사용자 이름으로 만들어진다
    @먼저("{string}가 사진 {string}을 올렸다")
    public void 사진을_올렸다(String userName, String photoName) {
        ExtractableResponse<Response> response =
                fileApi.requestPresignedUrl(zoo.tokenOf(userName), "zoo-mission.webp", "image/webp");
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        photoIds.put(photoName, response.jsonPath().getLong("fileId"));
    }

    @먼저("{string}가 {string} 조의 {word} 미션을 제출했다: 답 {string}, 사진 {string}")
    public void 미션을_제출했다(String userName, String teamName, String stopId, String answers, String photoName) {
        미션을_제출한다(userName, teamName, stopId, answers, photoName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    // --- 요청 ---

    @만일("{string}가 {string} 조의 {word} 미션을 제출한다: 답 {string}, 사진 {string}")
    public void 미션을_제출한다(String userName, String teamName, String stopId, String answers, String photoName) {
        submit(userName, teamName, stopId, Map.of("answers", parseAnswers(answers), "photoFileId", photoIdOf(photoName)));
    }

    @만일("{string}가 {string} 조의 {word} 미션을 사진 없이 제출한다: 답 {string}")
    public void 미션을_사진_없이_제출한다(String userName, String teamName, String stopId, String answers) {
        submit(userName, teamName, stopId, Map.of("answers", parseAnswers(answers)));
    }

    @만일("{string}가 {string} 조의 {word} 미션을 답 없이 제출한다: 사진 {string}")
    public void 미션을_답_없이_제출한다(String userName, String teamName, String stopId, String photoName) {
        submit(userName, teamName, stopId, Map.of("answers", List.of(), "photoFileId", photoIdOf(photoName)));
    }

    @만일("{string}가 {string} 조의 {word} 미션을 없는 사진으로 제출한다: 답 {string}")
    public void 미션을_없는_사진으로_제출한다(String userName, String teamName, String stopId, String answers) {
        submit(userName, teamName, stopId, Map.of("answers", parseAnswers(answers), "photoFileId", UNKNOWN_FILE_ID));
    }

    @만일("{string}가 전체 미션 제출 내용을 조회한다")
    public void 전체_미션_제출_내용을_조회한다(String userName) {
        world.response = zooTeamApi.getMissionResults(zoo.tokenOf(userName));
    }

    // --- 조 상세의 미션 확인 ---

    @그러면("응답한 조의 {word} 미션 답은 {string}이다")
    public void 응답한_조의_미션_답(String stopId, String answers) {
        assertThat(formatAnswers(missionOf(missions(), stopId))).isEqualTo(answers);
    }

    @그러면("응답한 조의 {word} 미션 사진은 {string}이다")
    public void 응답한_조의_미션_사진(String stopId, String photoName) {
        Map<String, Object> mission = missionOf(missions(), stopId);
        assertThat(((Number) mission.get("photoFileId")).longValue()).isEqualTo(photoIdOf(photoName));
        assertThat((String) mission.get("photoPath")).startsWith("uploads/");
    }

    @그러면("응답한 조의 미션은 {int}개이다")
    public void 응답한_조의_미션_수(int count) {
        assertThat(missions()).hasSize(count);
    }

    @그러면("응답한 조의 미션은 빈 배열이다")
    public void 응답한_조의_미션은_빈_배열이다() {
        Map<String, Object> team = world.response.jsonPath().getMap("$");
        assertThat(team).containsKey("missions");
        assertThat(missions()).isEmpty();
    }

    @그러면("응답한 조의 미션 키는 정확히 {string}이다")
    public void 응답한_조의_미션_키(String keys) {
        assertThat(missions()).isNotEmpty();
        assertThat(missions()).allSatisfy(mission -> assertThat(mission).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("응답한 조의 미션 답 키는 정확히 {string}이다")
    public void 응답한_조의_미션_답_키(String keys) {
        List<Map<String, Object>> answers = world.response.jsonPath().getList("missions[0].answers");
        assertThat(answers).isNotEmpty();
        assertThat(answers).allSatisfy(answer -> assertThat(answer).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("내 조의 미션은 {int}개이다")
    public void 내_조의_미션_수(int count) {
        assertThat(world.response.jsonPath().getList("team.missions")).hasSize(count);
    }

    // 두 번 다 조 상세를 다시 불러와 DB에 저장된 값끼리 비교한다
    @먼저("{string}가 {string} 조의 {word} 미션 시각을 기억해 둔다")
    public void 미션_시각을_기억해_둔다(String userName, String teamName, String stopId) {
        ExtractableResponse<Response> response = zooTeamApi.getTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
        Map<String, Object> mission = missionOf(response.jsonPath().getList("missions"), stopId);
        rememberedSubmittedAt = (String) mission.get("submittedAt");
        rememberedUpdatedAt = (String) mission.get("updatedAt");
        rememberedArrivedAt = arrivedAtOf(response, stopId);
    }

    @그러면("{string}가 {string} 조를 다시 보면 {word} 미션의 처음 제출 시각과 도착 시각은 그대로이고 수정 시각은 바뀌었다")
    public void 제출_시각과_도착_시각은_그대로이고_수정_시각은_바뀌었다(String userName, String teamName, String stopId) {
        ExtractableResponse<Response> response = zooTeamApi.getTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
        Map<String, Object> mission = missionOf(response.jsonPath().getList("missions"), stopId);

        assertThat(mission.get("submittedAt")).isEqualTo(rememberedSubmittedAt);
        assertThat(arrivedAtOf(response, stopId)).isEqualTo(rememberedArrivedAt);
        assertThat(LocalDateTime.parse((String) mission.get("updatedAt")))
                .isAfter(LocalDateTime.parse(rememberedUpdatedAt));
    }

    @그러면("미션과 답이 DB에 남아 있지 않다")
    public void 미션과_답이_DB에_남아_있지_않다() {
        assertThat(zooTeamMissionRepository.count()).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM zoo_team_mission_answer", Long.class)).isZero();
    }

    // --- 운영자 전체 조회 확인 ---

    @그러면("전체 제출 내용 항목의 키는 정확히 {string}이다")
    public void 전체_제출_내용_항목의_키(String keys) {
        List<Map<String, Object>> teams = world.response.jsonPath().getList("teams");
        assertThat(teams).isNotEmpty();
        assertThat(teams).allSatisfy(team -> assertThat(team).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("전체 제출 내용에서 {string} 조의 조원은 {string}이다")
    public void 전체_제출_내용의_조원(String teamName, String userNames) {
        List<String> expected = splitByComma(userNames).stream().map(zoo::currentNameOf).toList();
        assertThat(resultTeamOf(teamName).get("members")).isEqualTo(expected);
    }

    @그러면("전체 제출 내용에서 {string} 조의 {word} 미션 답은 {string}이다")
    public void 전체_제출_내용의_미션_답(String teamName, String stopId, String answers) {
        assertThat(formatAnswers(missionOf(missionsOf(resultTeamOf(teamName)), stopId))).isEqualTo(answers);
    }

    @그러면("전체 제출 내용에서 {string} 조의 미션은 {int}개이다")
    public void 전체_제출_내용의_미션_수(String teamName, int count) {
        assertThat(missionsOf(resultTeamOf(teamName))).hasSize(count);
    }

    private void submit(String userName, String teamName, String stopId, Map<String, Object> body) {
        world.response = zooTeamApi.submitMission(zoo.tokenOf(userName), zoo.teamIdOf(teamName), stopId, body);
    }

    private Long photoIdOf(String photoName) {
        return Objects.requireNonNull(photoIds.get(photoName), "시나리오에서 올리지 않은 사진: " + photoName);
    }

    // "q1=7개, q2=타조" → [{questionId: q1, answer: 7개}, ...]. 공백만 있는 답을 보내 보려고 답은 다듬지 않는다
    private List<Map<String, String>> parseAnswers(String answers) {
        return Arrays.stream(answers.split(","))
                .map(pair -> pair.split("=", 2))
                .map(pair -> Map.of("questionId", pair[0].strip(), "answer", pair[1]))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private String formatAnswers(Map<String, Object> mission) {
        List<Map<String, Object>> answers = (List<Map<String, Object>>) mission.get("answers");
        return answers.stream()
                .map(answer -> answer.get("questionId") + "=" + answer.get("answer"))
                .collect(Collectors.joining(", "));
    }

    private List<Map<String, Object>> missions() {
        return world.response.jsonPath().getList("missions");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> missionsOf(Map<String, Object> team) {
        return (List<Map<String, Object>>) team.get("missions");
    }

    private Map<String, Object> missionOf(List<Map<String, Object>> missions, String stopId) {
        return missions.stream()
                .filter(mission -> stopId.equals(mission.get("stopId")))
                .findFirst()
                .orElseThrow(() -> new AssertionError(stopId + " 미션이 없습니다."));
    }

    private String arrivedAtOf(ExtractableResponse<Response> response, String stopId) {
        List<Map<String, Object>> arrivals = response.jsonPath().getList("arrivals");
        return arrivals.stream()
                .filter(arrival -> stopId.equals(arrival.get("stopId")))
                .map(arrival -> (String) arrival.get("arrivedAt"))
                .findFirst()
                .orElseThrow(() -> new AssertionError(stopId + " 도착 기록이 없습니다."));
    }

    private Map<String, Object> resultTeamOf(String teamName) {
        Long teamId = zoo.teamIdOf(teamName);
        List<Map<String, Object>> teams = world.response.jsonPath().getList("teams");
        return teams.stream()
                .filter(team -> ((Number) team.get("id")).longValue() == teamId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("전체 제출 내용에 " + teamName + " 조가 없습니다."));
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .toList();
    }
}
