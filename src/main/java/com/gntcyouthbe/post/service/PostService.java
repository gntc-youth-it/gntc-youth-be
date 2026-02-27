package com.gntcyouthbe.post.service;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.file.service.FileStorageService;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.post.model.request.PostCreateRequest;
import com.gntcyouthbe.post.model.response.FeedResponse;
import com.gntcyouthbe.post.model.response.GalleryResponse;
import com.gntcyouthbe.post.model.response.PostResponse;
import com.gntcyouthbe.post.model.response.PostSubCategoryResponse;
import com.gntcyouthbe.post.repository.PostCommentRepository;
import com.gntcyouthbe.post.repository.PostImageRepository;
import com.gntcyouthbe.post.repository.PostLikeRepository;
import com.gntcyouthbe.post.repository.PostRepository;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.POST_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final FileStorageService fileStorageService;
    private final VerseRepository verseRepository;

    @Transactional(readOnly = true)
    public List<PostSubCategoryResponse> getSubCategories(PostCategory category) {
        List<PostSubCategory> subCategories = Arrays.stream(PostSubCategory.values())
                .filter(sub -> sub.getCategory() == category)
                .sorted(Comparator.comparing(
                        PostSubCategory::getStartDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        Map<PostSubCategory, Verse> verseMap = findVerses(subCategories);

        return subCategories.stream()
                .map(sub -> PostSubCategoryResponse.from(sub, verseMap.get(sub)))
                .toList();
    }

    private Map<PostSubCategory, Verse> findVerses(List<PostSubCategory> subCategories) {
        Map<PostSubCategory, Verse> verseMap = new EnumMap<>(PostSubCategory.class);
        for (PostSubCategory sub : subCategories) {
            if (sub.hasVerse()) {
                verseRepository.findByBook_BookNameAndChapterAndNumber(
                                sub.getBookName(), sub.getChapter(), sub.getVerseNumber())
                        .ifPresent(verse -> verseMap.put(sub, verse));
            }
        }
        return verseMap;
    }

    @Transactional
    public PostResponse createPost(UserPrincipal userPrincipal, PostCreateRequest request) {
        User author = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        PostStatus status = userPrincipal.getRole() == Role.MASTER
                ? PostStatus.APPROVED
                : PostStatus.PENDING_REVIEW;

        boolean isAuthorPublic = request.getIsAuthorPublic() != null && request.getIsAuthorPublic();
        Post post = new Post(author, request.getSubCategory(), status, request.getContent(), isAuthorPublic);
        post.updateHashtags(request.getHashtags());
        post.updateChurches(request.getChurches());
        addImages(post, request.getImageIds());

        postRepository.save(post);

        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(POST_NOT_FOUND));

        List<UploadedFile> uploadedFiles = post.getImages().stream()
                .map(PostImage::getUploadedFile)
                .toList();

        List<String> filePaths = uploadedFiles.stream()
                .map(UploadedFile::getFilePath)
                .toList();

        postLikeRepository.deleteByPostId(postId);
        postCommentRepository.deleteByPostId(postId);
        postRepository.delete(post);
        uploadedFileRepository.deleteAll(uploadedFiles);

        fileStorageService.deleteFiles(filePaths);
    }

    @Transactional(readOnly = true)
    public FeedResponse getFeed(PostSubCategory subCategory, ChurchId churchId, Long cursor, int size) {
        List<Post> posts = findFeedPosts(subCategory, churchId, cursor, size + 1);

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> commentCounts = postCommentRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return FeedResponse.of(posts, size, commentCounts);
    }

    @Transactional(readOnly = true)
    public GalleryResponse getGalleryImages(PostSubCategory subCategory, ChurchId churchId, Long cursor, int size) {
        List<PostImage> postImages = findGalleryImages(subCategory, churchId, cursor, size + 1);

        return GalleryResponse.of(postImages, size);
    }

    private List<Post> findFeedPosts(PostSubCategory subCategory, ChurchId churchId, Long cursor, int size) {
        if (subCategory != null && churchId != null) {
            return postRepository.findFeedBySubCategoryAndChurch(PostStatus.APPROVED, subCategory, churchId, cursor, size);
        }
        if (subCategory != null) {
            return postRepository.findFeedBySubCategory(PostStatus.APPROVED, subCategory, cursor, size);
        }
        if (churchId != null) {
            return postRepository.findFeedByChurch(PostStatus.APPROVED, churchId, cursor, size);
        }
        return postRepository.findFeed(PostStatus.APPROVED, cursor, size);
    }

    private List<PostImage> findGalleryImages(PostSubCategory subCategory, ChurchId churchId, Long cursor, int size) {
        if (subCategory != null && churchId != null) {
            return postImageRepository.findGalleryImagesBySubCategoryAndChurch(PostStatus.APPROVED, subCategory, churchId, cursor, size);
        }
        if (subCategory != null) {
            return postImageRepository.findGalleryImagesBySubCategory(PostStatus.APPROVED, subCategory, cursor, size);
        }
        if (churchId != null) {
            return postImageRepository.findGalleryImagesByChurch(PostStatus.APPROVED, churchId, cursor, size);
        }
        return postImageRepository.findGalleryImages(PostStatus.APPROVED, cursor, size);
    }

    private void addImages(Post post, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        List<UploadedFile> files = uploadedFileRepository.findAllById(imageIds);
        if (files.size() != imageIds.stream().distinct().count()) {
            throw new EntityNotFoundException(FILE_NOT_FOUND);
        }

        Map<Long, UploadedFile> fileMap = files.stream()
                .collect(Collectors.toMap(UploadedFile::getId, Function.identity()));

        for (int i = 0; i < imageIds.size(); i++) {
            post.addImage(new PostImage(fileMap.get(imageIds.get(i)), i + 1));
        }
    }
}
