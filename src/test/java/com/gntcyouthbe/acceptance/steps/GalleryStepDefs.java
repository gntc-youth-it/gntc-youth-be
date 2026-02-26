package com.gntcyouthbe.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.acceptance.support.api.PostApi;
import com.gntcyouthbe.acceptance.support.context.World;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.만일;
import java.util.List;
import org.springframework.http.HttpStatus;

public class GalleryStepDefs {

    private final World world;
    private final PostApi postApi;

    public GalleryStepDefs(World world, PostApi postApi) {
        this.world = world;
        this.postApi = postApi;
    }

    @만일("갤러리를 조회한다")
    public void 갤러리를_조회한다() {
        world.response = postApi.getGallery(null, null, null);
    }

    @그러면("승인된 게시글의 이미지만 반환된다")
    public void 승인된_게시글의_이미지만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> imageIds = world.response.jsonPath().getList("images.id", Long.class);
        // 승인된 게시글의 이미지 (901, 902, 903)만 포함
        assertThat(imageIds).contains(901L, 902L, 903L);
        // 검수대기 게시글의 이미지 (904)는 미포함
        assertThat(imageIds).doesNotContain(904L);
    }

    @만일("소분류 {string}로 갤러리를 조회한다")
    public void 소분류로_갤러리를_조회한다(String subCategory) {
        world.response = postApi.getGallery(subCategory, null, null);
    }

    @그러면("해당 소분류의 이미지만 반환된다")
    public void 해당_소분류의_이미지만_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<Long> imageIds = world.response.jsonPath().getList("images.id", Long.class);
        // RETREAT_2026_WINTER 소분류의 승인된 이미지만 포함 (901, 902)
        assertThat(imageIds).contains(901L, 902L);
        // NONE 소분류 이미지 (903)는 미포함
        assertThat(imageIds).doesNotContain(903L);
        // 검수대기 이미지 (904)도 미포함
        assertThat(imageIds).doesNotContain(904L);
    }

    @만일("size {int}로 갤러리를 조회한다")
    public void size로_갤러리를_조회한다(int size) {
        world.response = postApi.getGallery(null, null, size);
    }

    @그러면("{int}개의 이미지와 다음 페이지 정보가 반환된다")
    public void 개의_이미지와_다음_페이지_정보가_반환된다(int count) {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("images")).hasSize(count);
        assertThat(world.response.jsonPath().getBoolean("hasNext")).isTrue();
        assertThat(world.response.jsonPath().getLong("nextCursor")).isNotNull();
    }

    @만일("다음 커서로 갤러리를 조회한다")
    public void 다음_커서로_갤러리를_조회한다() {
        Long nextCursor = world.response.jsonPath().getLong("nextCursor");
        world.response = postApi.getGallery(null, nextCursor, 1);
    }

    @그러면("다음 이미지가 반환된다")
    public void 다음_이미지가_반환된다() {
        assertThat(world.response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(world.response.jsonPath().getList("images")).hasSize(1);
    }
}
