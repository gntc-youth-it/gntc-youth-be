package com.gntcyouthbe.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.post.model.request.PostCreateRequest;
import com.gntcyouthbe.post.model.response.GalleryResponse;
import com.gntcyouthbe.post.model.response.PostResponse;
import com.gntcyouthbe.post.repository.PostImageRepository;
import com.gntcyouthbe.post.repository.PostRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("일반 사용자가 게시글을 작성하면 PENDING_REVIEW 상태로 생성된다")
    void createPost_userRole_pendingReview() {
        // given
        User user = createUser(1L, "테스트유저", Role.USER);
        UserPrincipal principal = new UserPrincipal(user);
        PostCreateRequest request = new PostCreateRequest(
                PostSubCategory.RETREAT_2026_WINTER, "수련회 후기", null, null, null, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 1L);
            return post;
        });

        // when
        PostResponse response = postService.createPost(principal, request);

        // then
        assertThat(response.getStatus()).isEqualTo(PostStatus.PENDING_REVIEW);
        assertThat(response.getContent()).isEqualTo("수련회 후기");
        assertThat(response.getSubCategory()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER);
    }

    @Test
    @DisplayName("MASTER가 게시글을 작성하면 APPROVED 상태로 생성된다")
    void createPost_masterRole_approved() {
        // given
        User master = createUser(2L, "마스터유저", Role.MASTER);
        UserPrincipal principal = new UserPrincipal(master);
        PostCreateRequest request = new PostCreateRequest(
                PostSubCategory.RETREAT_2026_WINTER, "마스터 게시글", null, null, null, null);

        given(userRepository.findById(2L)).willReturn(Optional.of(master));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 2L);
            return post;
        });

        // when
        PostResponse response = postService.createPost(principal, request);

        // then
        assertThat(response.getStatus()).isEqualTo(PostStatus.APPROVED);
    }

    @Test
    @DisplayName("해시태그와 성전 태그가 정상적으로 저장된다")
    void createPost_withHashtagsAndChurches() {
        // given
        User user = createUser(1L, "테스트유저", Role.USER);
        UserPrincipal principal = new UserPrincipal(user);
        PostCreateRequest request = new PostCreateRequest(
                PostSubCategory.RETREAT_2026_WINTER,
                "합동 수련회",
                List.of("수련회", "은혜"),
                List.of(ChurchId.ANYANG, ChurchId.SUWON),
                null, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 1L);
            return post;
        });

        // when
        PostResponse response = postService.createPost(principal, request);

        // then
        assertThat(response.getHashtags()).containsExactly("수련회", "은혜");
        assertThat(response.getChurches()).containsExactly(ChurchId.ANYANG, ChurchId.SUWON);
    }

    @Test
    @DisplayName("이미지 ID 목록을 전달하면 게시글에 이미지가 순서대로 연결된다")
    void createPost_withImages() {
        // given
        User user = createUser(1L, "테스트유저", Role.USER);
        UserPrincipal principal = new UserPrincipal(user);
        PostCreateRequest request = new PostCreateRequest(
                PostSubCategory.RETREAT_2026_WINTER, "이미지 게시글", null, null,
                List.of(10L, 20L), null);

        UploadedFile file1 = createUploadedFile(10L, "photo1.jpg", "images/photo1.jpg");
        UploadedFile file2 = createUploadedFile(20L, "photo2.jpg", "images/photo2.jpg");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(uploadedFileRepository.findAllById(List.of(10L, 20L))).willReturn(List.of(file1, file2));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 1L);
            return post;
        });

        // when
        PostResponse response = postService.createPost(principal, request);

        // then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.getImages().get(0).getFileId()).isEqualTo(10L);
        assertThat(response.getImages().get(0).getSortOrder()).isEqualTo(1);
        assertThat(response.getImages().get(1).getFileId()).isEqualTo(20L);
        assertThat(response.getImages().get(1).getSortOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 이미지 ID를 전달하면 예외가 발생한다")
    void createPost_withInvalidImageId_throwsException() {
        // given
        User user = createUser(1L, "테스트유저", Role.USER);
        UserPrincipal principal = new UserPrincipal(user);
        PostCreateRequest request = new PostCreateRequest(
                PostSubCategory.RETREAT_2026_WINTER, "게시글", null, null,
                List.of(999L), null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(uploadedFileRepository.findAllById(List.of(999L))).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> postService.createPost(principal, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("소분류 없이 갤러리를 조회하면 전체 승인된 이미지가 반환된다")
    void getGalleryImages_withoutSubCategory() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(3L, "uploads/c.jpg"),
                createPostImage(2L, "uploads/b.jpg")
        );
        given(postImageRepository.findGalleryImages(Long.MAX_VALUE, 21))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(null, Long.MAX_VALUE, 20);

        // then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("소분류를 지정하면 해당 소분류의 승인된 이미지만 반환된다")
    void getGalleryImages_withSubCategory() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(2L, "uploads/b.jpg"),
                createPostImage(1L, "uploads/a.jpg")
        );
        given(postImageRepository.findGalleryImagesBySubCategory(
                PostSubCategory.RETREAT_2026_WINTER, Long.MAX_VALUE, 21))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(
                PostSubCategory.RETREAT_2026_WINTER, Long.MAX_VALUE, 20);

        // then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("결과가 size보다 많으면 hasNext가 true이다")
    void getGalleryImages_hasNextTrue() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(3L, "uploads/c.jpg"),
                createPostImage(2L, "uploads/b.jpg"),
                createPostImage(1L, "uploads/a.jpg")
        );
        given(postImageRepository.findGalleryImages(Long.MAX_VALUE, 3))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(null, Long.MAX_VALUE, 2);

        // then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(2L);
    }

    private User createUser(Long id, String name, Role role) {
        User user = new User("email@test.com", name, AuthProvider.KAKAO, "provider_" + id);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }

    private UploadedFile createUploadedFile(Long id, String originalFilename, String filePath) {
        UploadedFile file = new UploadedFile(originalFilename, "stored_" + originalFilename,
                filePath, "image/jpeg", 1024L);
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }

    private PostImage createPostImage(Long id, String filePath) {
        UploadedFile file = createUploadedFile(id, "photo.jpg", filePath);
        PostImage postImage = new PostImage(file, 1);
        ReflectionTestUtils.setField(postImage, "id", id);
        return postImage;
    }
}
