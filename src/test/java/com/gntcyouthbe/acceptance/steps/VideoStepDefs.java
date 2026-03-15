package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.VideoApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class VideoStepDefs {

    private final World world;
    private final AuthApi authApi;
    private final VideoApi videoApi;

    public VideoStepDefs(World world, AuthApi authApi, VideoApi videoApi) {
        this.world = world;
        this.authApi = authApi;
        this.videoApi = videoApi;
    }

    // --- 영상 등록 ---

    @먼저("마스터가 영상 등록을 위해 로그인되어 있다")
    public void 마스터가_영상_등록을_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("master@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @먼저("일반 사용자가 영상 등록을 위해 로그인되어 있다")
    public void 일반_사용자가_영상_등록을_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("test@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("마스터가 영상을 등록한다")
    public void 마스터가_영상을_등록한다() {
        world.response = videoApi.createVideo(world.authToken, Map.of(
                "title", "수련회 찬양 영상",
                "link", "https://www.youtube.com/watch?v=test123",
                "subCategory", "RETREAT_2026_WINTER"
        ));
    }

    @그러면("영상이 성공적으로 등록된다")
    public void 영상이_성공적으로_등록된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(world.response.jsonPath().getString("title")).isEqualTo("수련회 찬양 영상");
        assertThat(world.response.jsonPath().getString("link")).isEqualTo("https://www.youtube.com/watch?v=test123");
        assertThat(world.response.jsonPath().getString("subCategory")).isEqualTo("RETREAT_2026_WINTER");
    }

    @만일("일반 사용자가 영상을 등록한다")
    public void 일반_사용자가_영상을_등록한다() {
        world.response = videoApi.createVideo(world.authToken, Map.of(
                "title", "테스트 영상",
                "link", "https://www.youtube.com/watch?v=test456",
                "subCategory", "RETREAT_2026_WINTER"
        ));
    }

    @그러면("영상 등록 권한 에러가 반환된다")
    public void 영상_등록_권한_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @만일("미인증 사용자가 영상을 등록한다")
    public void 미인증_사용자가_영상을_등록한다() {
        world.response = videoApi.createVideoWithoutAuth(Map.of(
                "title", "미인증 영상",
                "link", "https://www.youtube.com/watch?v=test789",
                "subCategory", "RETREAT_2026_WINTER"
        ));
    }

    @그러면("영상 등록 인증 에러가 반환된다")
    public void 영상_등록_인증_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    // --- 영상 조회 ---

    @만일("전체 영상 목록을 조회한다")
    public void 전체_영상_목록을_조회한다() {
        world.response = videoApi.getVideos(null);
    }

    @그러면("영상 목록이 반환된다")
    public void 영상_목록이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @만일("행사별 영상 목록을 조회한다")
    public void 행사별_영상_목록을_조회한다() {
        world.response = videoApi.getVideos("RETREAT_2026_WINTER");
    }

    @그러면("해당 행사의 영상만 반환된다")
    public void 해당_행사의_영상만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<String> subCategories = world.response.jsonPath().getList("subCategory", String.class);
        assertThat(subCategories).allSatisfy(
                subCategory -> assertThat(subCategory).isEqualTo("RETREAT_2026_WINTER")
        );
    }

    // --- 영상 삭제 ---

    @먼저("마스터가 영상 삭제를 위해 로그인되어 있다")
    public void 마스터가_영상_삭제를_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("master@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @먼저("일반 사용자가 영상 삭제를 위해 로그인되어 있다")
    public void 일반_사용자가_영상_삭제를_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("test@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("마스터가 영상을 삭제한다")
    public void 마스터가_영상을_삭제한다() {
        world.response = videoApi.deleteVideo(world.authToken, 1001L);
    }

    @그러면("영상이 삭제된다")
    public void 영상이_삭제된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @그리고("삭제된 영상은 목록에서 조회되지 않는다")
    public void 삭제된_영상은_목록에서_조회되지_않는다() {
        world.response = videoApi.getVideos(null);
        List<Long> videoIds = world.response.jsonPath().getList("id", Long.class);
        assertThat(videoIds).doesNotContain(1001L);
    }

    @만일("일반 사용자가 영상 삭제를 시도한다")
    public void 일반_사용자가_영상_삭제를_시도한다() {
        world.response = videoApi.deleteVideo(world.authToken, 1002L);
    }

    @만일("미인증 사용자가 영상 삭제를 시도한다")
    public void 미인증_사용자가_영상_삭제를_시도한다() {
        world.response = videoApi.deleteVideoWithoutAuth(1002L);
    }

    @그러면("영상 삭제 권한 에러가 반환된다")
    public void 영상_삭제_권한_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @그러면("영상 삭제 인증 에러가 반환된다")
    public void 영상_삭제_인증_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @만일("마스터가 존재하지 않는 영상을 삭제한다")
    public void 마스터가_존재하지_않는_영상을_삭제한다() {
        world.response = videoApi.deleteVideo(world.authToken, 99999L);
    }
}
