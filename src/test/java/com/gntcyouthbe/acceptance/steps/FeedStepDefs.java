package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.PostApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.List;
import org.springframework.http.HttpStatus;

public class FeedStepDefs {

    private final World world;
    private final AuthApi authApi;
    private final PostApi postApi;

    public FeedStepDefs(World world, AuthApi authApi, PostApi postApi) {
        this.world = world;
        this.authApi = authApi;
        this.postApi = postApi;
    }

    @만일("피드를 조회한다")
    public void 피드를_조회한다() {
        world.response = postApi.getFeed(null, null, null, null);
    }

    @그러면("승인된 게시글만 반환된다")
    public void 승인된_게시글만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> postIds = world.response.jsonPath().getList("posts.id", Long.class);
        // 승인된 게시글 (901, 902)만 포함
        assertThat(postIds).contains(901L, 902L);
        // 검수대기 게시글 (903)는 미포함
        assertThat(postIds).doesNotContain(903L);

        // 마스터유저(id=3)의 프로필 이미지 URL이 포함된다
        List<String> profileImageUrls = world.response.jsonPath()
                .getList("posts.authorProfileImageUrl", String.class);
        assertThat(profileImageUrls).allMatch(url -> url.equals("uploads/profile.jpg"));
    }

    @만일("소분류 {string}로 피드를 조회한다")
    public void 소분류로_피드를_조회한다(String subCategory) {
        world.response = postApi.getFeed(subCategory, null, null, null);
    }

    @그러면("해당 소분류의 게시글만 반환된다")
    public void 해당_소분류의_게시글만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> postIds = world.response.jsonPath().getList("posts.id", Long.class);
        // RETREAT_2026_WINTER 소분류의 승인된 게시글만 포함 (901)
        assertThat(postIds).contains(901L);
        // NONE 소분류 게시글 (902)는 미포함
        assertThat(postIds).doesNotContain(902L);
        // 검수대기 게시글 (903)도 미포함
        assertThat(postIds).doesNotContain(903L);
    }

    @만일("성전 {string}으로 피드를 조회한다")
    public void 성전으로_피드를_조회한다(String churchId) {
        world.response = postApi.getFeed(null, churchId, null, null);
    }

    @그러면("해당 성전의 게시글만 반환된다")
    public void 해당_성전의_게시글만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> postIds = world.response.jsonPath().getList("posts.id", Long.class);
        // ANYANG 성전 태그된 게시글만 포함 (901)
        assertThat(postIds).contains(901L);
        // SUWON 성전 태그된 게시글 (902)는 미포함
        assertThat(postIds).doesNotContain(902L);
    }

    @만일("소분류 {string}와 성전 {string}으로 피드를 조회한다")
    public void 소분류와_성전으로_피드를_조회한다(String subCategory, String churchId) {
        world.response = postApi.getFeed(subCategory, churchId, null, null);
    }

    @그러면("해당 소분류와 성전의 게시글만 반환된다")
    public void 해당_소분류와_성전의_게시글만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> postIds = world.response.jsonPath().getList("posts.id", Long.class);
        // RETREAT_2026_WINTER + ANYANG 조합: 게시글 901만 해당
        assertThat(postIds).containsExactly(901L);
    }

    @만일("size {int}로 피드를 조회한다")
    public void size로_피드를_조회한다(int size) {
        world.response = postApi.getFeed(null, null, null, size);
    }

    @그러면("{int}개의 게시글과 다음 페이지 정보가 반환된다")
    public void 개의_게시글과_다음_페이지_정보가_반환된다(int count) {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("posts")).hasSize(count);
        assertThat(world.response.jsonPath().getBoolean("hasNext")).isTrue();
        assertThat(world.response.jsonPath().getLong("nextCursor")).isNotNull();
    }

    @만일("다음 커서로 피드를 조회한다")
    public void 다음_커서로_피드를_조회한다() {
        Long nextCursor = world.response.jsonPath().getLong("nextCursor");
        world.response = postApi.getFeed(null, null, nextCursor, 1);
    }

    @그러면("다음 게시글이 반환된다")
    public void 다음_게시글이_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("posts")).hasSize(1);
    }

    // --- 검수대기 피드 조회 ---

    @먼저("마스터가 검수대기 피드 조회를 위해 로그인되어 있다")
    public void 마스터가_검수대기_피드_조회를_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("master@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @먼저("일반 사용자가 검수대기 피드 조회를 위해 로그인되어 있다")
    public void 일반_사용자가_검수대기_피드_조회를_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("test@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("마스터가 검수대기 피드를 조회한다")
    public void 마스터가_검수대기_피드를_조회한다() {
        world.response = postApi.getPendingFeed(world.authToken, null, null);
    }

    @그러면("검수대기 게시글만 반환된다")
    public void 검수대기_게시글만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> postIds = world.response.jsonPath().getList("posts.id", Long.class);
        // 검수대기 게시글 (903)만 포함
        assertThat(postIds).contains(903L);
        // 승인된 게시글 (901, 902)는 미포함
        assertThat(postIds).doesNotContain(901L, 902L);

        List<String> statuses = world.response.jsonPath().getList("posts.status", String.class);
        assertThat(statuses).allMatch(status -> status.equals("PENDING_REVIEW"));
    }

    @만일("미인증 사용자가 검수대기 피드를 조회한다")
    public void 미인증_사용자가_검수대기_피드를_조회한다() {
        world.response = postApi.getPendingFeedWithoutAuth(null, null);
    }

    @만일("일반 사용자가 검수대기 피드를 조회한다")
    public void 일반_사용자가_검수대기_피드를_조회한다() {
        world.response = postApi.getPendingFeed(world.authToken, null, null);
    }

    @그러면("검수대기 피드 인증 에러가 반환된다")
    public void 검수대기_피드_인증_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    // --- 게시글 승인 ---

    @만일("마스터가 검수대기 게시글을 승인한다")
    public void 마스터가_검수대기_게시글을_승인한다() {
        world.response = postApi.approvePost(world.authToken, 905L);
    }

    @그러면("게시글이 승인 처리된다")
    public void 게시글이_승인_처리된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @만일("미인증 사용자가 게시글을 승인한다")
    public void 미인증_사용자가_게시글을_승인한다() {
        world.response = postApi.approvePostWithoutAuth(905L);
    }

    @만일("일반 사용자가 게시글을 승인한다")
    public void 일반_사용자가_게시글을_승인한다() {
        world.response = postApi.approvePost(world.authToken, 905L);
    }
}
