package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.ZooPhotoApi;
import com.gntcyouthbe.acceptance.support.context.World;
import com.gntcyouthbe.acceptance.support.context.ZooScenarioContext;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.repository.ZooPhotoVoteRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ZooPhotoStepDefs {

    private static final Long UNKNOWN_PHOTO_ID = 999_999L;

    private final World world;
    private final ZooScenarioContext zoo;
    private final ZooPhotoApi zooPhotoApi;
    private final ZooTeamMissionRepository zooTeamMissionRepository;
    private final ZooPhotoVoteRepository zooPhotoVoteRepository;

    public ZooPhotoStepDefs(World world, ZooScenarioContext zoo, ZooPhotoApi zooPhotoApi,
            ZooTeamMissionRepository zooTeamMissionRepository, ZooPhotoVoteRepository zooPhotoVoteRepository) {
        this.world = world;
        this.zoo = zoo;
        this.zooPhotoApi = zooPhotoApi;
        this.zooTeamMissionRepository = zooTeamMissionRepository;
        this.zooPhotoVoteRepository = zooPhotoVoteRepository;
    }

    // --- 요청 ---

    @먼저("{string}가 {string} 조의 {word} 사진에 투표했다")
    public void 사진에_투표했다(String userName, String teamName, String stopId) {
        사진에_투표한다(userName, teamName, stopId);
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @만일("{string}가 {string} 조의 {word} 사진에 투표한다")
    public void 사진에_투표한다(String userName, String teamName, String stopId) {
        world.response = zooPhotoApi.vote(zoo.tokenOf(userName), photoIdOf(teamName, stopId));
    }

    @만일("{string}가 {string} 조의 {word} 사진 투표를 취소한다")
    public void 사진_투표를_취소한다(String userName, String teamName, String stopId) {
        world.response = zooPhotoApi.cancelVote(zoo.tokenOf(userName), photoIdOf(teamName, stopId));
    }

    @만일("{string}가 없는 사진에 투표한다")
    public void 없는_사진에_투표한다(String userName) {
        world.response = zooPhotoApi.vote(zoo.tokenOf(userName), UNKNOWN_PHOTO_ID);
    }

    @만일("{string}가 사진 목록을 조회한다")
    public void 사진_목록을_조회한다(String userName) {
        world.response = zooPhotoApi.getPhotos(zoo.tokenOf(userName));
    }

    @만일("{string}가 사진 투표 결과를 조회한다")
    public void 사진_투표_결과를_조회한다(String userName) {
        world.response = zooPhotoApi.getResults(zoo.tokenOf(userName));
    }

    // --- 사진 목록 확인 ---

    @그러면("사진 목록의 사진은 {int}장이다")
    public void 사진_목록의_사진_수(int count) {
        assertThat(photos()).hasSize(count);
    }

    @그러면("사진 목록 항목의 키는 정확히 {string}이다")
    public void 사진_목록_항목의_키(String keys) {
        assertThat(photos()).isNotEmpty();
        assertThat(photos()).allSatisfy(photo -> assertThat(photo).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("사진 목록에서 {string} 조의 {word} 사진은 내 조 사진으로 표시된다")
    public void 내_조_사진으로_표시된다(String teamName, String stopId) {
        assertThat(photoOf(teamName, stopId).get("isMyTeam")).isEqualTo(true);
    }

    @그러면("사진 목록에서 {string} 조의 {word} 사진은 내 조 사진이 아닌 것으로 표시된다")
    public void 내_조_사진이_아닌_것으로_표시된다(String teamName, String stopId) {
        assertThat(photoOf(teamName, stopId).get("isMyTeam")).isEqualTo(false);
    }

    @그러면("사진 목록에서 {string} 조의 {word} 사진은 투표한 것으로 표시된다")
    public void 투표한_것으로_표시된다(String teamName, String stopId) {
        assertThat(photoOf(teamName, stopId).get("isVoted")).isEqualTo(true);
    }

    @그러면("사진 목록에서 {string} 조의 {word} 사진은 투표하지 않은 것으로 표시된다")
    public void 투표하지_않은_것으로_표시된다(String teamName, String stopId) {
        assertThat(photoOf(teamName, stopId).get("isVoted")).isEqualTo(false);
    }

    // --- 결과 확인 ---

    @그러면("투표 결과 항목의 키는 정확히 {string}이다")
    public void 투표_결과_항목의_키(String keys) {
        assertThat(photos()).isNotEmpty();
        assertThat(photos()).allSatisfy(photo -> assertThat(photo).containsOnlyKeys(splitByComma(keys)));
    }

    @그러면("투표 결과의 투표한 사람 수는 {int}명이다")
    public void 투표한_사람_수(int count) {
        assertThat(world.response.jsonPath().getLong("voterCount")).isEqualTo(count);
    }

    @그러면("투표 결과에서 {string} 조의 {word} 사진은 {int}표이다")
    public void 사진의_득표_수(String teamName, String stopId, int voteCount) {
        assertThat(((Number) photoOf(teamName, stopId).get("voteCount")).intValue()).isEqualTo(voteCount);
    }

    // "호랑이팀 AFRICA_1, 사자팀 AFRICA_1" 처럼 조 이름과 장소를 적는다
    @그러면("투표 결과의 사진 순서는 {string}이다")
    public void 투표_결과의_사진_순서(String photoNames) {
        List<Long> expected = splitByComma(photoNames).stream()
                .map(name -> name.split(" "))
                .map(teamAndStop -> photoIdOf(teamAndStop[0], teamAndStop[1]))
                .toList();
        assertThat(world.response.jsonPath().getList("photos.id", Long.class)).containsExactlyElementsOf(expected);
    }

    @그러면("사진 투표는 {int}개 남아 있다")
    public void 사진_투표는_남아_있다(int count) {
        assertThat(zooPhotoVoteRepository.count()).isEqualTo(count);
    }

    // 사진 id는 미션 id다
    private Long photoIdOf(String teamName, String stopId) {
        return zooTeamMissionRepository.findByTeamIdAndStop(zoo.teamIdOf(teamName), ZooStop.valueOf(stopId))
                .orElseThrow(() -> new AssertionError(teamName + " 조의 " + stopId + " 미션이 없습니다."))
                .getId();
    }

    private List<Map<String, Object>> photos() {
        return world.response.jsonPath().getList("photos");
    }

    private Map<String, Object> photoOf(String teamName, String stopId) {
        Long photoId = photoIdOf(teamName, stopId);
        return photos().stream()
                .filter(photo -> ((Number) photo.get("id")).longValue() == photoId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("응답에 " + teamName + " 조의 " + stopId + " 사진이 없습니다."));
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .toList();
    }
}
