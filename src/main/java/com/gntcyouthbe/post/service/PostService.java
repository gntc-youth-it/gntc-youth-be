package com.gntcyouthbe.post.service;

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
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final UploadedFileRepository uploadedFileRepository;

    @Transactional
    public PostResponse createPost(UserPrincipal userPrincipal, PostCreateRequest request) {
        User author = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        PostStatus status = userPrincipal.getRole() == Role.MASTER
                ? PostStatus.APPROVED
                : PostStatus.PENDING_REVIEW;

        Post post = new Post(author, request.getSubCategory(), status, request.getContent());
        post.updateHashtags(request.getHashtags());
        post.updateChurches(request.getChurches());
        addImages(post, request.getImageIds());

        postRepository.save(post);

        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public GalleryResponse getGalleryImages(PostSubCategory subCategory, Long cursor, int size) {
        List<PostImage> postImages = (subCategory != null)
                ? postImageRepository.findGalleryImagesBySubCategory(subCategory, cursor, size + 1)
                : postImageRepository.findGalleryImages(cursor, size + 1);

        return GalleryResponse.of(postImages, size);
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
