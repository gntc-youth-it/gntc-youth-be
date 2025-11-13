package com.gntcyouthbe.bible.controller;

import com.gntcyouthbe.bible.model.response.*;
import com.gntcyouthbe.bible.service.RankService;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bible/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping
    public ResponseEntity<RankResponse> getRank() {
        return ResponseEntity.ok(rankService.getRank());
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyRankResponse> getMyRank(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(rankService.getMyRank(userPrincipal));
    }

    @GetMapping("/recent")
    public ResponseEntity<RecentRankResponse> getRecentRank() {
        return ResponseEntity.ok(rankService.getRecentRank());
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyRankResponse> getDailyRank() {
        return ResponseEntity.ok(rankService.getDailyRank());
    }

    @GetMapping("/daily/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyRankResponse> getMyDailyRank(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(rankService.getMyDailyRank(userPrincipal));
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyRankResponse> getWeeklyRank() {
        return ResponseEntity.ok(rankService.getWeeklyRank());
    }

    @GetMapping("/weekly/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyRankResponse> getMyWeeklyRank(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(rankService.getMyWeeklyRank(userPrincipal));
    }

}
