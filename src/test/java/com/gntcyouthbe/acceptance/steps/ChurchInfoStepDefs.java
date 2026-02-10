package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.ChurchInfoApi;
import com.gntcyouthbe.acceptance.support.api.FileApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class ChurchInfoStepDefs {

    private final World world;
    private final AuthApi authApi;
    private final FileApi fileApi;
    private final ChurchInfoApi churchInfoApi;

    public ChurchInfoStepDefs(World world, AuthApi authApi, FileApi fileApi, ChurchInfoApi churchInfoApi) {
        this.world = world;
        this.authApi = authApi;
        this.fileApi = fileApi;
        this.churchInfoApi = churchInfoApi;
    }

    @먼저("리더가 로그인되어 있다")
    public void 리더가_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("leader@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("리더가 파일 업로드 URL을 요청한다")
    public void 리더가_파일_업로드_URL을_요청한다() {
        world.response = fileApi.requestPresignedUrl(world.authToken, "group_photo.jpg", "image/jpeg");
    }

    @그러면("Presigned URL이 반환된다")
    public void presigned_url이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getLong("fileId")).isPositive();
        assertThat(world.response.jsonPath().getString("presignedUrl")).isNotBlank();
    }

    @만일("일반 사용자가 파일 업로드 URL을 요청한다")
    public void 일반_사용자가_파일_업로드_URL을_요청한다() {
        world.response = fileApi.requestPresignedUrl(world.authToken, "group_photo.jpg", "image/jpeg");
    }

    @만일("리더가 성전 정보를 저장한다")
    public void 리더가_성전_정보를_저장한다() {
        Long fileId = world.response.jsonPath().getLong("fileId");
        world.response = churchInfoApi.saveChurchInfo(
                world.authToken,
                "ANYANG",
                fileId,
                List.of(
                        Map.of("content", "교회의 부흥을 위해", "sortOrder", 1),
                        Map.of("content", "청년들의 신앙 성장을 위해", "sortOrder", 2)
                )
        );
    }

    @그러면("성전 정보 저장이 성공한다")
    public void 성전_정보_저장이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("ANYANG");
        assertThat(world.response.jsonPath().getList("prayerTopics")).hasSize(2);
        assertThat(world.response.jsonPath().getString("prayerTopics[0].content")).isEqualTo("교회의 부흥을 위해");
        assertThat(world.response.jsonPath().getString("prayerTopics[1].content")).isEqualTo("청년들의 신앙 성장을 위해");
    }

    @만일("사용자가 성전 정보를 조회한다")
    public void 사용자가_성전_정보를_조회한다() {
        world.response = churchInfoApi.getChurchInfo("ANYANG");
    }

    @그러면("성전 정보가 반환된다")
    public void 성전_정보가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("ANYANG");
        assertThat(world.response.jsonPath().getString("groupPhotoPath")).isNotBlank();
        assertThat(world.response.jsonPath().getList("prayerTopics")).hasSize(2);
    }

    @만일("리더가 성전 정보를 수정한다")
    public void 리더가_성전_정보를_수정한다() {
        world.response = churchInfoApi.saveChurchInfo(
                world.authToken,
                "ANYANG",
                null,
                List.of(
                        Map.of("content", "수정된 기도제목", "sortOrder", 1)
                )
        );
    }

    @그러면("성전 정보 수정이 성공한다")
    public void 성전_정보_수정이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("prayerTopics")).hasSize(1);
        assertThat(world.response.jsonPath().getString("prayerTopics[0].content")).isEqualTo("수정된 기도제목");
    }

    @만일("일반 사용자가 성전 정보를 저장한다")
    public void 일반_사용자가_성전_정보를_저장한다() {
        world.response = churchInfoApi.saveChurchInfo(
                world.authToken,
                "ANYANG",
                null,
                List.of(Map.of("content", "기도제목", "sortOrder", 1))
        );
    }

    @만일("리더가 다른 성전 정보를 저장한다")
    public void 리더가_다른_성전_정보를_저장한다() {
        world.response = churchInfoApi.saveChurchInfo(
                world.authToken,
                "SUWON",
                null,
                List.of(Map.of("content", "기도제목", "sortOrder", 1))
        );
    }

    @그러면("권한 에러가 반환된다")
    public void 권한_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @먼저("마스터가 로그인되어 있다")
    public void 마스터가_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("master@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("마스터가 다른 성전 정보를 저장한다")
    public void 마스터가_다른_성전_정보를_저장한다() {
        world.response = churchInfoApi.saveChurchInfo(
                world.authToken,
                "SUWON",
                null,
                List.of(Map.of("content", "마스터 기도제목", "sortOrder", 1))
        );
    }

    @그러면("다른 성전 정보 저장이 성공한다")
    public void 다른_성전_정보_저장이_성공한다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("churchId")).isEqualTo("SUWON");
    }
}
