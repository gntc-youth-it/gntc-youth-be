package com.gntcyouthbe.post.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.model.request.PostCreateRequest;
import com.gntcyouthbe.post.model.response.PostResponse;
import com.gntcyouthbe.post.repository.PostRepository;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponse createPost(UserPrincipal userPrincipal, PostCreateRequest request) {
        User author = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        PostStatus status = userPrincipal.getRole() == Role.MASTER
                ? PostStatus.APPROVED
                : PostStatus.PENDING_REVIEW;

        Post post = new Post(author, request.getSubCategory(), status, request.getContent());
        post.updateHashtags(request.getHashtags());
        post.updateChurches(request.getChurchIds());

        postRepository.save(post);

        return PostResponse.from(post);
    }
}
