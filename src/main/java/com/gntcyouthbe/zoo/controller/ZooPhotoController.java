package com.gntcyouthbe.zoo.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.zoo.model.response.ZooPhotoListResponse;
import com.gntcyouthbe.zoo.model.response.ZooPhotoResultResponse;
import com.gntcyouthbe.zoo.service.ZooPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zoo/photos")
@RequiredArgsConstructor
public class ZooPhotoController {

    private final ZooPhotoService zooPhotoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooPhotoListResponse> getPhotos(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(zooPhotoService.getPhotos(userPrincipal));
    }

    // 이미 투표했어도 204다
    @PutMapping("/{photoId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> vote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long photoId) {
        zooPhotoService.vote(userPrincipal, photoId);
        return ResponseEntity.noContent().build();
    }

    // 투표하지 않았어도 204다
    @DeleteMapping("/{photoId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelVote(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long photoId) {
        zooPhotoService.cancelVote(userPrincipal, photoId);
        return ResponseEntity.noContent().build();
    }

    // 득표 수는 MASTER만 본다. 아니면 서비스에서 403(5008)으로 거절한다
    @GetMapping("/results")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooPhotoResultResponse> getResults(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(zooPhotoService.getResults(userPrincipal));
    }
}
