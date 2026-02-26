package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.AuthApi;
import com.gntcyouthbe.acceptance.support.api.PostApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.먼저;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class PostStepDefs {

    private final World world;
    private final AuthApi authApi;
    private final PostApi postApi;

    public PostStepDefs(World world, AuthApi authApi, PostApi postApi) {
        this.world = world;
        this.authApi = authApi;
        this.postApi = postApi;
    }

    @먼저("일반 사용자가 로그인되어 있다")
    public void 일반_사용자가_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("test@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @먼저("마스터가 게시글 작성을 위해 로그인되어 있다")
    public void 마스터가_게시글_작성을_위해_로그인되어_있다() {
        world.authToken = authApi.getAccessToken("master@example.com");
        assertThat(world.authToken).isNotBlank();
    }

    @만일("사용자가 게시글을 작성한다")
    public void 사용자가_게시글을_작성한다() {
        world.response = postApi.createPost(world.authToken, Map.of(
                "subCategory", "RETREAT_2026_WINTER",
                "content", "수련회 후기입니다"
        ));
    }

    @그러면("게시글이 심사대기 상태로 생성된다")
    public void 게시글이_심사대기_상태로_생성된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("status")).isEqualTo("PENDING_REVIEW");
        assertThat(world.response.jsonPath().getString("content")).isEqualTo("수련회 후기입니다");
        assertThat(world.response.jsonPath().getString("subCategory")).isEqualTo("RETREAT_2026_WINTER");
        assertThat(world.response.jsonPath().getString("category")).isEqualTo("RETREAT");
    }

    @만일("마스터가 게시글을 작성한다")
    public void 마스터가_게시글을_작성한다() {
        world.response = postApi.createPost(world.authToken, Map.of(
                "subCategory", "RETREAT_2026_WINTER",
                "content", "마스터 게시글입니다"
        ));
    }

    @그러면("게시글이 승인 상태로 생성된다")
    public void 게시글이_승인_상태로_생성된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getString("status")).isEqualTo("APPROVED");
    }

    @만일("사용자가 성전 태그와 해시태그를 포함하여 게시글을 작성한다")
    public void 사용자가_성전_태그와_해시태그를_포함하여_게시글을_작성한다() {
        world.response = postApi.createPost(world.authToken, Map.of(
                "subCategory", "RETREAT_2026_WINTER",
                "content", "안양 수원 합동 수련회",
                "hashtags", List.of("수련회", "은혜"),
                "churchIds", List.of("ANYANG", "SUWON")
        ));
    }

    @그러면("게시글에 성전 태그와 해시태그가 포함되어 있다")
    public void 게시글에_성전_태그와_해시태그가_포함되어_있다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("hashtags")).containsExactly("수련회", "은혜");
        assertThat(world.response.jsonPath().getList("churchIds")).containsExactly("ANYANG", "SUWON");
    }

    @만일("미인증 사용자가 게시글을 작성한다")
    public void 미인증_사용자가_게시글을_작성한다() {
        world.response = postApi.createPostWithoutAuth(Map.of(
                "subCategory", "RETREAT_2026_WINTER",
                "content", "미인증 게시글"
        ));
    }

    @그러면("게시글 작성 인증 에러가 반환된다")
    public void 게시글_작성_인증_에러가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
