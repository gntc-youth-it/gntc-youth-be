package com.gntcyouthbe.thanks.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.model.request.ThanksCommentCreateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCommentListResponse;
import com.gntcyouthbe.thanks.service.ThanksCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thanks")
@RequiredArgsConstructor
public class ThanksCommentController {

    private final ThanksCommentService thanksCommentService;

    // 오래된 순(채팅처럼 새 댓글이 아래)으로 전부 준다. 한 감사에 많아야 수십 개라 페이지를 나누지 않는다
    @GetMapping("/entries/{entryId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksCommentListResponse> getComments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long entryId) {
        return ResponseEntity.ok(thanksCommentService.getComments(userPrincipal, entryId));
    }

    // 201과 함께 그 감사의 댓글 전체를 돌려준다
    @PostMapping("/entries/{entryId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksCommentListResponse> addComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long entryId,
            @Valid @RequestBody ThanksCommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(thanksCommentService.addComment(userPrincipal, entryId, request));
    }

    // 쓴 사람 본인이나 MASTER만 지운다. 200과 함께 그 감사의 남은 댓글 전체를 돌려준다
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksCommentListResponse> deleteComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(thanksCommentService.deleteComment(userPrincipal, commentId));
    }
}
