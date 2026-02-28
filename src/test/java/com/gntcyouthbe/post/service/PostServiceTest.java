package com.gntcyouthbe.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.file.service.FileStorageService;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.post.model.request.PostCreateRequest;
import com.gntcyouthbe.post.model.response.FeedResponse;
import com.gntcyouthbe.post.model.response.GalleryResponse;
import com.gntcyouthbe.post.model.response.PostResponse;
import com.gntcyouthbe.post.repository.PostCommentRepository;
import com.gntcyouthbe.post.repository.PostImageRepository;
import com.gntcyouthbe.post.repository.PostLikeRepository;
import com.gntcyouthbe.post.repository.PostRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Gender;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.repository.UserProfileRepository;
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
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("일반 사용자가 게시글을 작성하면 PENDING_REVIEW 상태로 생성된다")
    void createPost_userRole_pendingReview() {
        // given
        User user = createUser(1L, "테스트유저", Role.USER);
        UserPrincipal principal = new UserPrincipal(user, null);
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
        UserPrincipal principal = new UserPrincipal(master, null);
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
        UserPrincipal principal = new UserPrincipal(user, null);
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
        UserPrincipal principal = new UserPrincipal(user, null);
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
        UserPrincipal principal = new UserPrincipal(user, null);
        PostCreateRequest request = new PostCreateRequest(
                PostSubCategory.RETREAT_2026_WINTER, "게시글", null, null,
                List.of(999L), null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(uploadedFileRepository.findAllById(List.of(999L))).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> postService.createPost(principal, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- 갤러리 조회 테스트 ---

    @Test
    @DisplayName("소분류 없이 갤러리를 조회하면 전체 승인된 이미지가 반환된다")
    void getGalleryImages_withoutSubCategory() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(3L, "uploads/c.jpg"),
                createPostImage(2L, "uploads/b.jpg")
        );
        given(postImageRepository.findGalleryImages(PostStatus.APPROVED, Long.MAX_VALUE, 21))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(null, null, Long.MAX_VALUE, 20);

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
                PostStatus.APPROVED, PostSubCategory.RETREAT_2026_WINTER, Long.MAX_VALUE, 21))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(
                PostSubCategory.RETREAT_2026_WINTER, null, Long.MAX_VALUE, 20);

        // then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("성전을 지정하면 해당 성전의 승인된 이미지만 반환된다")
    void getGalleryImages_withChurchId() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(2L, "uploads/b.jpg")
        );
        given(postImageRepository.findGalleryImagesByChurch(
                PostStatus.APPROVED, ChurchId.ANYANG, Long.MAX_VALUE, 21))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(
                null, ChurchId.ANYANG, Long.MAX_VALUE, 20);

        // then
        assertThat(response.getImages()).hasSize(1);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("소분류와 성전을 동시에 지정하면 두 조건 모두 만족하는 이미지가 반환된다")
    void getGalleryImages_withSubCategoryAndChurchId() {
        // given
        List<PostImage> postImages = List.of(
                createPostImage(1L, "uploads/a.jpg")
        );
        given(postImageRepository.findGalleryImagesBySubCategoryAndChurch(
                PostStatus.APPROVED, PostSubCategory.RETREAT_2026_WINTER, ChurchId.ANYANG, Long.MAX_VALUE, 21))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(
                PostSubCategory.RETREAT_2026_WINTER, ChurchId.ANYANG, Long.MAX_VALUE, 20);

        // then
        assertThat(response.getImages()).hasSize(1);
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
        given(postImageRepository.findGalleryImages(PostStatus.APPROVED, Long.MAX_VALUE, 3))
                .willReturn(postImages);

        // when
        GalleryResponse response = postService.getGalleryImages(null, null, Long.MAX_VALUE, 2);

        // then
        assertThat(response.getImages()).hasSize(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(2L);
    }

    // --- 검수대기 피드 조회 테스트 ---

    @Test
    @DisplayName("검수대기 피드를 조회하면 PENDING_REVIEW 상태의 게시글이 반환된다")
    void getPendingFeed_returnsPendingReviewPosts() {
        // given
        User author = createUser(1L, "작성자", Role.USER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER, PostStatus.PENDING_REVIEW);

        given(postRepository.findFeed(PostStatus.PENDING_REVIEW, Long.MAX_VALUE, 5))
                .willReturn(List.of(post));
        given(postCommentRepository.countByPostIds(List.of(10L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of());

        // when
        FeedResponse response = postService.getPendingFeed(Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.getPosts().get(0).getStatus()).isEqualTo(PostStatus.PENDING_REVIEW);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("검수대기 피드 조회 시 작성자의 프로필 이미지 URL이 포함된다")
    void getPendingFeed_withAuthorProfileImage() {
        // given
        User author = createUser(1L, "작성자", Role.USER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER, PostStatus.PENDING_REVIEW);

        UploadedFile profileImage = createUploadedFile(50L, "profile.jpg", "uploads/profile.jpg");
        UserProfile profile = new UserProfile(author, 45, "010-1234-5678", Gender.MALE);
        profile.updateProfileImage(profileImage);

        given(postRepository.findFeed(PostStatus.PENDING_REVIEW, Long.MAX_VALUE, 5))
                .willReturn(List.of(post));
        given(postCommentRepository.countByPostIds(List.of(10L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of(profile));

        // when
        FeedResponse response = postService.getPendingFeed(Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.getPosts().get(0).getAuthorProfileImageUrl()).isEqualTo("uploads/profile.jpg");
    }

    @Test
    @DisplayName("검수대기 피드 결과가 size보다 많으면 hasNext가 true이다")
    void getPendingFeed_hasNextTrue() {
        // given
        User author = createUser(1L, "작성자", Role.USER);
        Post post1 = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER, PostStatus.PENDING_REVIEW);
        Post post2 = createPost(9L, author, PostSubCategory.NONE, PostStatus.PENDING_REVIEW);
        Post post3 = createPost(8L, author, PostSubCategory.NONE, PostStatus.PENDING_REVIEW);

        given(postRepository.findFeed(PostStatus.PENDING_REVIEW, Long.MAX_VALUE, 3))
                .willReturn(List.of(post1, post2, post3));
        given(postCommentRepository.countByPostIds(List.of(10L, 9L, 8L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of());

        // when
        FeedResponse response = postService.getPendingFeed(Long.MAX_VALUE, 2);

        // then
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(9L);
    }

    // --- 피드 조회 테스트 ---

    @Test
    @DisplayName("필터 없이 피드를 조회하면 전체 승인된 게시글이 반환된다")
    void getFeed_withoutFilter() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post1 = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER);
        Post post2 = createPost(9L, author, PostSubCategory.NONE);

        given(postRepository.findFeed(PostStatus.APPROVED, Long.MAX_VALUE, 5))
                .willReturn(List.of(post1, post2));
        given(postCommentRepository.countByPostIds(List.of(10L, 9L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of());

        // when
        FeedResponse response = postService.getFeed(null, null, Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("피드 조회 시 작성자의 프로필 이미지 URL이 포함된다")
    void getFeed_withAuthorProfileImage() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER);

        UploadedFile profileImage = createUploadedFile(50L, "profile.jpg", "uploads/profile.jpg");
        UserProfile profile = new UserProfile(author, 45, "010-1234-5678", Gender.MALE);
        profile.updateProfileImage(profileImage);

        given(postRepository.findFeed(PostStatus.APPROVED, Long.MAX_VALUE, 5))
                .willReturn(List.of(post));
        given(postCommentRepository.countByPostIds(List.of(10L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of(profile));

        // when
        FeedResponse response = postService.getFeed(null, null, Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.getPosts().get(0).getAuthorProfileImageUrl()).isEqualTo("uploads/profile.jpg");
    }

    @Test
    @DisplayName("프로필 이미지가 없는 작성자는 null이 반환된다")
    void getFeed_withoutAuthorProfileImage() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER);

        UserProfile profile = new UserProfile(author, 45, "010-1234-5678", Gender.MALE);

        given(postRepository.findFeed(PostStatus.APPROVED, Long.MAX_VALUE, 5))
                .willReturn(List.of(post));
        given(postCommentRepository.countByPostIds(List.of(10L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of(profile));

        // when
        FeedResponse response = postService.getFeed(null, null, Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.getPosts().get(0).getAuthorProfileImageUrl()).isNull();
    }

    @Test
    @DisplayName("성전을 지정하면 해당 성전의 게시글만 반환된다")
    void getFeed_withChurchId() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER);

        given(postRepository.findFeedByChurch(PostStatus.APPROVED, ChurchId.ANYANG, Long.MAX_VALUE, 5))
                .willReturn(List.of(post));
        given(postCommentRepository.countByPostIds(List.of(10L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of());

        // when
        FeedResponse response = postService.getFeed(null, ChurchId.ANYANG, Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("소분류와 성전을 동시에 지정하면 두 조건 모두 만족하는 게시글이 반환된다")
    void getFeed_withSubCategoryAndChurchId() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER);

        given(postRepository.findFeedBySubCategoryAndChurch(
                PostStatus.APPROVED, PostSubCategory.RETREAT_2026_WINTER, ChurchId.ANYANG, Long.MAX_VALUE, 5))
                .willReturn(List.of(post));
        given(postCommentRepository.countByPostIds(List.of(10L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of());

        // when
        FeedResponse response = postService.getFeed(
                PostSubCategory.RETREAT_2026_WINTER, ChurchId.ANYANG, Long.MAX_VALUE, 4);

        // then
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("피드 결과가 size보다 많으면 hasNext가 true이다")
    void getFeed_hasNextTrue() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post1 = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER);
        Post post2 = createPost(9L, author, PostSubCategory.NONE);
        Post post3 = createPost(8L, author, PostSubCategory.NONE);

        given(postRepository.findFeed(PostStatus.APPROVED, Long.MAX_VALUE, 3))
                .willReturn(List.of(post1, post2, post3));
        given(postCommentRepository.countByPostIds(List.of(10L, 9L, 8L)))
                .willReturn(List.of());
        given(userProfileRepository.findByUserIdInWithProfileImage(List.of(1L)))
                .willReturn(List.of());

        // when
        FeedResponse response = postService.getFeed(null, null, Long.MAX_VALUE, 2);

        // then
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(9L);
    }

    // --- 게시글 승인 테스트 ---

    @Test
    @DisplayName("검수대기 게시글을 승인하면 APPROVED 상태로 변경된다")
    void approvePost_pendingReview_changesStatusToApproved() {
        // given
        User author = createUser(1L, "작성자", Role.USER);
        Post post = createPost(10L, author, PostSubCategory.RETREAT_2026_WINTER, PostStatus.PENDING_REVIEW);

        given(postRepository.findById(10L)).willReturn(Optional.of(post));

        // when
        postService.approvePost(10L);

        // then
        assertThat(post.getStatus()).isEqualTo(PostStatus.APPROVED);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 승인하면 예외가 발생한다")
    void approvePost_notFound_throwsException() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.approvePost(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- 게시글 삭제 테스트 ---

    @Test
    @DisplayName("게시글을 삭제하면 좋아요, 댓글, 게시글, 파일이 모두 삭제된다")
    void deletePost_withImages_deletesAll() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post = createPost(100L, author, PostSubCategory.RETREAT_2026_WINTER);

        UploadedFile file1 = createUploadedFile(10L, "photo1.jpg", "uploads/stored_photo1.jpg");
        UploadedFile file2 = createUploadedFile(20L, "photo2.jpg", "uploads/stored_photo2.jpg");
        post.addImage(new PostImage(file1, 1));
        post.addImage(new PostImage(file2, 2));

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(100L);

        // then
        then(postLikeRepository).should().deleteByPostId(100L);
        then(postCommentRepository).should().deleteByPostId(100L);
        then(postRepository).should().delete(post);
        then(uploadedFileRepository).should().deleteAll(List.of(file1, file2));
        then(fileStorageService).should().deleteFiles(List.of("uploads/stored_photo1.jpg", "uploads/stored_photo2.jpg"));
    }

    @Test
    @DisplayName("이미지가 없는 게시글도 정상적으로 삭제된다")
    void deletePost_withoutImages_deletesPost() {
        // given
        User author = createUser(1L, "작성자", Role.MASTER);
        Post post = createPost(100L, author, PostSubCategory.NONE);

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(100L);

        // then
        then(postLikeRepository).should().deleteByPostId(100L);
        then(postCommentRepository).should().deleteByPostId(100L);
        then(postRepository).should().delete(post);
        then(uploadedFileRepository).should().deleteAll(List.of());
        then(fileStorageService).should().deleteFiles(List.of());
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 삭제하면 예외가 발생한다")
    void deletePost_notFound_throwsException() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(999L))
                .isInstanceOf(EntityNotFoundException.class);

        then(postRepository).should(never()).delete(any());
        then(fileStorageService).should(never()).deleteFiles(any());
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

    private Post createPost(Long id, User author, PostSubCategory subCategory) {
        return createPost(id, author, subCategory, PostStatus.APPROVED);
    }

    private Post createPost(Long id, User author, PostSubCategory subCategory, PostStatus status) {
        Post post = new Post(author, subCategory, status, "테스트 게시글", false);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
