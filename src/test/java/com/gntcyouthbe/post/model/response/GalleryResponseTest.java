package com.gntcyouthbe.post.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GalleryResponseTest {

    @Test
    @DisplayName("이미지 수가 size보다 많으면 hasNext가 true이고 nextCursor가 반환된다")
    void of_hasNext_true() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(3L, "uploads/c.jpg"),
                createPostImage(2L, "uploads/b.jpg"),
                createPostImage(1L, "uploads/a.jpg")
        );

        // when
        GalleryResponse response = GalleryResponse.of(postImages, 2);

        // then
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(2L);
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getImages().get(0).getId()).isEqualTo(3L);
        assertThat(response.getImages().get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이미지 수가 size 이하이면 hasNext가 false이고 nextCursor가 null이다")
    void of_hasNext_false() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(2L, "uploads/b.jpg"),
                createPostImage(1L, "uploads/a.jpg")
        );

        // when
        GalleryResponse response = GalleryResponse.of(postImages, 2);

        // then
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getImages()).hasSize(2);
    }

    @Test
    @DisplayName("빈 이미지 목록이면 빈 응답이 반환된다")
    void of_emptyList() {
        // when
        GalleryResponse response = GalleryResponse.of(List.of(), 20);

        // then
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
        assertThat(response.getImages()).isEmpty();
    }

    @Test
    @DisplayName("이미지 응답에 id와 url이 올바르게 매핑된다")
    void of_imageResponseMapping() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(5L, "uploads/photo.jpg")
        );

        // when
        GalleryResponse response = GalleryResponse.of(postImages, 10);

        // then
        assertThat(response.getImages()).hasSize(1);
        assertThat(response.getImages().getFirst().getId()).isEqualTo(5L);
        assertThat(response.getImages().getFirst().getUrl()).isEqualTo("uploads/photo.jpg");
    }

    private PostImage createPostImage(Long id, String filePath) {
        User user = new User("test@test.com", "테스트", AuthProvider.KAKAO, "provider_1");
        ReflectionTestUtils.setField(user, "id", 1L);

        Post post = new Post(user, PostSubCategory.RETREAT_2026_WINTER, PostStatus.APPROVED, "내용", false);
        ReflectionTestUtils.setField(post, "id", 1L);

        UploadedFile file = new UploadedFile("original.jpg", "stored.jpg", filePath, "image/jpeg", 1024L);
        ReflectionTestUtils.setField(file, "id", id);

        PostImage postImage = new PostImage(file, 1);
        ReflectionTestUtils.setField(postImage, "id", id);
        ReflectionTestUtils.setField(postImage, "post", post);

        return postImage;
    }
}
