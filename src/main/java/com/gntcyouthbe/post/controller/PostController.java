package com.gntcyouthbe.post.controller;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.post.model.request.PostCreateRequest;
import com.gntcyouthbe.post.model.response.FeedResponse;
import com.gntcyouthbe.post.model.response.GalleryResponse;
import com.gntcyouthbe.post.model.response.PostCategoryResponse;
import com.gntcyouthbe.post.model.response.PostResponse;
import com.gntcyouthbe.post.model.response.PostSubCategoryResponse;
import com.gntcyouthbe.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/categories")
    public ResponseEntity<List<PostCategoryResponse>> getCategories() {
        return ResponseEntity.ok(PostCategoryResponse.fromAll());
    }

    @GetMapping("/categories/{category}/sub-categories")
    public ResponseEntity<List<PostSubCategoryResponse>> getSubCategories(
            @PathVariable PostCategory category) {
        return ResponseEntity.ok(PostSubCategoryResponse.fromCategory(category));
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedResponse> getFeed(
            @RequestParam(required = false) PostSubCategory subCategory,
            @RequestParam(required = false) ChurchId churchId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "4") int size) {
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        return ResponseEntity.ok(postService.getFeed(subCategory, churchId, cursor, size));
    }

    @GetMapping("/gallery")
    public ResponseEntity<GalleryResponse> getGalleryImages(
            @RequestParam(required = false) PostSubCategory subCategory,
            @RequestParam(required = false) ChurchId churchId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        return ResponseEntity.ok(postService.getGalleryImages(subCategory, churchId, cursor, size));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(userPrincipal, request));
    }
}
