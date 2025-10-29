package com.gntcyouthbe.bible.controller;

import com.gntcyouthbe.bible.model.response.RecentChapterResponse;
import com.gntcyouthbe.bible.model.response.VerseCopyResponse;
import com.gntcyouthbe.bible.service.CopyService;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book/copy")
@RequiredArgsConstructor
public class CopyController {

    private final CopyService copyService;

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecentChapterResponse> getRecentChapter(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(copyService.getRecentChapter(userPrincipal));
    }

    @PostMapping("/{verseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerseCopyResponse> copyVerse(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long verseId
    ) {
        copyService.copyVerse(userPrincipal, verseId);
        return ResponseEntity.ok(VerseCopyResponse.success());
    }
}
