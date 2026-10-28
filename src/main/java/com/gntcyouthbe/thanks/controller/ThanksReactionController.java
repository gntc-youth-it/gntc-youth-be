package com.gntcyouthbe.thanks.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksEmoji;
import com.gntcyouthbe.thanks.model.response.ThanksEntryReactionsResponse;
import com.gntcyouthbe.thanks.service.ThanksReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// emoji가 5가지 중 하나가 아니면 GlobalExceptionHandler가 1001로 응답한다
@RestController
@RequestMapping("/thanks/entries/{entryId}/reactions")
@RequiredArgsConstructor
public class ThanksReactionController {

    private final ThanksReactionService thanksReactionService;

    // 이미 켜져 있어도 200이다. 그 감사의 반응 전체를 돌려준다
    @PutMapping("/{emoji}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksEntryReactionsResponse> addReaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long entryId,
            @PathVariable ThanksEmoji emoji) {
        return ResponseEntity.ok(thanksReactionService.addReaction(userPrincipal, entryId, emoji));
    }

    // 켜져 있지 않아도 200이다
    @DeleteMapping("/{emoji}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksEntryReactionsResponse> removeReaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long entryId,
            @PathVariable ThanksEmoji emoji) {
        return ResponseEntity.ok(thanksReactionService.removeReaction(userPrincipal, entryId, emoji));
    }
}
