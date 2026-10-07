package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ZooTeamApi;
import com.gntcyouthbe.acceptance.support.context.World;
import com.gntcyouthbe.acceptance.support.context.ZooScenarioContext;
import com.gntcyouthbe.zoo.repository.ZooPhotoVoteRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamArrivalRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMemberRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamRepository;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ZooTeamStepDefs {

    private static final Long UNKNOWN_TEAM_ID = 999_999L;

    private final World world;
    private final ZooScenarioContext zoo;
    private final ZooTeamApi zooTeamApi;
    private final ZooTeamRepository zooTeamRepository;
    private final ZooTeamMemberRepository zooTeamMemberRepository;
    private final ZooTeamArrivalRepository zooTeamArrivalRepository;
    private final ZooTeamMissionRepository zooTeamMissionRepository;
    private final ZooPhotoVoteRepository zooPhotoVoteRepository;

    private LocalDateTime firstStartedAt;

    public ZooTeamStepDefs(World world, ZooScenarioContext zoo, ZooTeamApi zooTeamApi,
            ZooTeamRepository zooTeamRepository,
            ZooTeamMemberRepository zooTeamMemberRepository,
            ZooTeamArrivalRepository zooTeamArrivalRepository,
            ZooTeamMissionRepository zooTeamMissionRepository,
            ZooPhotoVoteRepository zooPhotoVoteRepository) {
        this.world = world;
        this.zoo = zoo;
        this.zooTeamApi = zooTeamApi;
        this.zooTeamRepository = zooTeamRepository;
        this.zooTeamMemberRepository = zooTeamMemberRepository;
        this.zooTeamArrivalRepository = zooTeamArrivalRepository;
        this.zooTeamMissionRepository = zooTeamMissionRepository;
        this.zooPhotoVoteRepository = zooPhotoVoteRepository;
    }

    // 시나리오끼리 DB를 같이 쓰므로, 한 사람은 한 조에만 들어갈 수 있는 제약에 걸리지 않게 매번 비운다
    @Before("@zoo")
    public void 동물원_조_데이터를_비운다() {
        zooPhotoVoteRepository.deleteAllInBatch();
        zooTeamMissionRepository.deleteAll(); // 답(@ElementCollection)까지 지우려면 일괄 삭제가 아니라 엔티티로 지운다
        zooTeamArrivalRepository.deleteAllInBatch();
        zooTeamMemberRepository.deleteAllInBatch();
        zooTeamRepository.deleteAllInBatch();
    }

    // --- 준비 ---

    @먼저("{string}가 {word} 코스로 {string} 조를 만들었다")
    public void 조를_만들었다(String userName, String course, String teamName) {
        조를_만든다(userName, course, teamName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @먼저("{string}가 {string} 조에 참여했다")
    public void 조에_참여했다(String userName, String teamName) {
        조에_참여한다(userName, teamName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @먼저("{string}가 {string} 조를 마감했다")
    public void 조를_마감했다(String userName, String teamName) {
        조를_마감한다(userName, teamName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @먼저("{string}가 {string} 조에 {word} 도착을 기록했다")
    public void 도착을_기록했다(String userName, String teamName, String stopId) {
        도착을_기록한다(userName, teamName, stopId);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @먼저("{string}가 {string} 조의 조장을 {string}에게 넘겼다")
    public void 조장을_넘겼다(String userName, String teamName, String targetUserName) {
        조장을_넘긴다(userName, teamName, targetUserName);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    // --- 요청 ---

    @만일("{string}가 {word} 코스로 {string} 조를 만든다")
    public void 조를_만든다(String userName, String course, String teamName) {
        world.response = zooTeamApi.createTeam(zoo.tokenOf(userName), teamName, course);
        if (world.response.statusCode() == HttpStatus.CREATED.value()) {
            zoo.saveTeamId(teamName, world.response.jsonPath().getLong("id"));
        }
    }

    @만일("{string}가 {string} 조에 참여한다")
    public void 조에_참여한다(String userName, String teamName) {
        world.response = zooTeamApi.joinTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
    }

    @만일("{string}가 {string} 조에서 나간다")
    public void 조에서_나간다(String userName, String teamName) {
        world.response = zooTeamApi.leaveTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
    }

    @만일("{string}가 {string} 조의 코스를 {word}로 바꾼다")
    public void 코스를_바꾼다(String userName, String teamName, String course) {
        world.response = zooTeamApi.changeCourse(zoo.tokenOf(userName), zoo.teamIdOf(teamName), course);
    }

    @만일("{string}가 {string} 조를 마감한다")
    public void 조를_마감한다(String userName, String teamName) {
        world.response = zooTeamApi.startTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
    }

    @만일("{string}가 {string} 조에 {word} 도착을 기록한다")
    public void 도착을_기록한다(String userName, String teamName, String stopId) {
        world.response = zooTeamApi.markArrival(zoo.tokenOf(userName), zoo.teamIdOf(teamName), stopId);
    }

    @만일("{string}가 {string} 조의 {word} 도착을 취소한다")
    public void 도착을_취소한다(String userName, String teamName, String stopId) {
        world.response = zooTeamApi.cancelArrival(zoo.tokenOf(userName), zoo.teamIdOf(teamName), stopId);
    }

    @만일("{string}가 {string} 조의 조장을 {string}에게 넘긴다")
    public void 조장을_넘긴다(String userName, String teamName, String targetUserName) {
        world.response = zooTeamApi.transferLeader(zoo.tokenOf(userName), zoo.teamIdOf(teamName), zoo.userIdOf(targetUserName));
    }

    @만일("{string}가 {string} 조를 삭제한다")
    public void 조를_삭제한다(String userName, String teamName) {
        world.response = zooTeamApi.deleteTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
    }

    @만일("{string}가 {string} 조를 조회한다")
    public void 조를_조회한다(String userName, String teamName) {
        world.response = zooTeamApi.getTeam(zoo.tokenOf(userName), zoo.teamIdOf(teamName));
    }

    @만일("{string}가 내 조를 조회한다")
    public void 내_조를_조회한다(String userName) {
        world.response = zooTeamApi.getMyTeam(zoo.tokenOf(userName));
    }

    @만일("{string}가 조 목록을 조회한다")
    public void 조_목록을_조회한다(String userName) {
        world.response = zooTeamApi.getTeams(zoo.tokenOf(userName));
    }

    @만일("{string}가 없는 조를 조회한다")
    public void 없는_조를_조회한다(String userName) {
        world.response = zooTeamApi.getTeam(zoo.tokenOf(userName), UNKNOWN_TEAM_ID);
    }

    @만일("미인증 사용자가 조 목록을 조회한다")
    public void 미인증_사용자가_조_목록을_조회한다() {
        world.response = zooTeamApi.getTeamsWithoutAuth();
    }

    @만일("미인증 사용자가 조를 만든다")
    public void 미인증_사용자가_조를_만든다() {
        world.response = zooTeamApi.createTeamWithoutAuth("사자팀", "A");
    }

    // --- 조 상세 확인 ---

    @그러면("응답한 조의 상태는 {word}이고 코스는 {word}이다")
    public void 응답한_조의_상태와_코스(String status, String course) {
        assertThat(world.response.jsonPath().getString("status")).isEqualTo(status);
        assertThat(world.response.jsonPath().getString("course")).isEqualTo(course);
    }

    @그러면("응답한 조의 이름은 {string}이다")
    public void 응답한_조의_이름(String name) {
        assertThat(world.response.jsonPath().getString("name")).isEqualTo(name);
    }

    @그러면("응답한 조의 조장은 {string}이다")
    public void 응답한_조의_조장(String userName) {
        Long userId = zoo.userIdOf(userName);
        assertThat(world.response.jsonPath().getLong("leaderUserId")).isEqualTo(userId);
        assertThat(members())
                .filteredOn(member -> Boolean.TRUE.equals(member.get("isLeader")))
                .extracting(member -> ((Number) member.get("userId")).longValue())
                .containsExactly(userId);
    }

    @그러면("응답한 조의 조원은 {int}명이다")
    public void 응답한_조의_조원_수(int count) {
        assertThat(members()).hasSize(count);
    }

    @그러면("응답한 조의 조원 중에 {string}가 있다")
    public void 응답한_조의_조원_중에_있다(String userName) {
        assertThat(world.response.jsonPath().getList("members.userId", Long.class)).contains(zoo.userIdOf(userName));
    }

    @그러면("응답한 조의 출발 시각이 기록되어 있다")
    public void 응답한_조의_출발_시각이_기록되어_있다() {
        String startedAt = world.response.jsonPath().getString("startedAt");
        assertThat(startedAt).isNotNull();
        firstStartedAt = LocalDateTime.parse(startedAt);
    }

    // 처음 응답은 메모리 값(나노초), 이후 응답은 DB 값(마이크로초)이라 1마이크로초까지는 같은 시각으로 본다
    @그러면("응답한 조의 출발 시각은 처음 마감한 시각 그대로이다")
    public void 응답한_조의_출발_시각은_그대로이다() {
        LocalDateTime startedAt = LocalDateTime.parse(world.response.jsonPath().getString("startedAt"));
        assertThat(Duration.between(firstStartedAt, startedAt).abs()).isLessThanOrEqualTo(Duration.ofNanos(1_000));
    }

    @그러면("응답한 조의 도착 기록은 {string}이다")
    public void 응답한_조의_도착_기록(String stopIds) {
        assertThat(world.response.jsonPath().getList("arrivals.stopId", String.class))
                .containsExactlyInAnyOrderElementsOf(splitByComma(stopIds));
    }

    @그러면("응답한 조의 키는 정확히 {string}이다")
    public void 응답한_조의_키(String keys) {
        Map<String, Object> team = world.response.jsonPath().getMap("$");
        assertThat(team).containsOnlyKeys(splitByComma(keys));
    }

    @그러면("응답한 조의 조원 정보 키는 정확히 {string}이다")
    public void 응답한_조의_조원_정보_키(String keys) {
        assertThat(members()).allSatisfy(member -> assertThat(member).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("응답한 조의 도착 기록 키는 정확히 {string}이다")
    public void 응답한_조의_도착_기록_키(String keys) {
        List<Map<String, Object>> arrivals = world.response.jsonPath().getList("arrivals");
        assertThat(arrivals).isNotEmpty();
        assertThat(arrivals).allSatisfy(arrival -> assertThat(arrival).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("응답한 조에서 {string}의 프로필 이미지는 {string}이다")
    public void 프로필_이미지는(String userName, String profileImagePath) {
        assertThat(memberOf(userName).get("profileImagePath")).isEqualTo(profileImagePath);
    }

    @그러면("응답한 조에서 {string}의 프로필 이미지는 없다")
    public void 프로필_이미지는_없다(String userName) {
        Map<String, Object> member = memberOf(userName);
        assertThat(member).containsKey("profileImagePath");
        assertThat(member.get("profileImagePath")).isNull();
    }

    // --- 내 조, 삭제 확인 ---

    @그러면("{string}는 속한 조가 없다")
    public void 속한_조가_없다(String userName) {
        ExtractableResponse<Response> response = zooTeamApi.getMyTeam(zoo.tokenOf(userName));
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getMap("$")).containsEntry("team", null);
    }

    @그러면("{string} 조는 더 이상 조회되지 않는다")
    public void 조는_더_이상_조회되지_않는다(String teamName) {
        ExtractableResponse<Response> response = zooTeamApi.getTeam(zoo.tokenOf("안양유저"), zoo.teamIdOf(teamName));
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.jsonPath().getInt("code")).isEqualTo(5001);
    }

    @그러면("내 조 응답의 team은 null이다")
    public void 내_조_응답의_team은_null이다() {
        Map<String, Object> body = world.response.jsonPath().getMap("$");
        assertThat(body).containsOnlyKeys("team");
        assertThat(body.get("team")).isNull();
    }

    @그러면("내 조는 {string}이다")
    public void 내_조는(String teamName) {
        assertThat(world.response.jsonPath().getLong("team.id")).isEqualTo(zoo.teamIdOf(teamName));
        assertThat(world.response.jsonPath().getString("team.name")).isEqualTo(teamName);
    }

    // --- 조 목록 확인 ---

    @그러면("조 목록 항목의 키는 정확히 {string}이다")
    public void 조_목록_항목의_키(String keys) {
        List<Map<String, Object>> teams = world.response.jsonPath().getList("teams");
        assertThat(teams).isNotEmpty();
        assertThat(teams).allSatisfy(team -> assertThat(team).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("조 목록에서 {string} 조는 조장 {string}, 조원 {int}명, 도착 {int}곳이다")
    public void 조_목록의_조_요약(String teamName, String leaderUserName, int memberCount, int arrivedCount) {
        Long teamId = zoo.teamIdOf(teamName);
        List<Map<String, Object>> teams = world.response.jsonPath().getList("teams");
        Map<String, Object> team = teams.stream()
                .filter(summary -> ((Number) summary.get("id")).longValue() == teamId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("조 목록에 " + teamName + " 조가 없습니다."));

        assertThat(team.get("leaderName")).isEqualTo(zoo.currentNameOf(leaderUserName));
        assertThat(((Number) team.get("memberCount")).intValue()).isEqualTo(memberCount);
        assertThat(((Number) team.get("arrivedCount")).intValue()).isEqualTo(arrivedCount);
    }

    private List<Map<String, Object>> members() {
        return world.response.jsonPath().getList("members");
    }

    private Map<String, Object> memberOf(String userName) {
        Long userId = zoo.userIdOf(userName);
        return members().stream()
                .filter(member -> ((Number) member.get("userId")).longValue() == userId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("조원 목록에 " + userName + "가 없습니다."));
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .toList();
    }
}
