package com.gntcyouthbe.video.controller;

import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.video.model.request.VideoCreateRequest;
import com.gntcyouthbe.video.model.response.VideoResponse;
import com.gntcyouthbe.video.service.VideoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<VideoResponse>> getVideos(
            @RequestParam(required = false) PostSubCategory subCategory) {
        return ResponseEntity.ok(videoService.getVideos(subCategory));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MASTER')")
    public ResponseEntity<VideoResponse> createVideo(
            @Valid @RequestBody VideoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(videoService.createVideo(request));
    }

    @DeleteMapping("/{videoId}")
    @PreAuthorize("hasAuthority('MASTER')")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return ResponseEntity.noContent().build();
    }
}
