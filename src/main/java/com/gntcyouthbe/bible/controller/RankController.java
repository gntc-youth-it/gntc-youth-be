package com.gntcyouthbe.bible.controller;

import com.gntcyouthbe.bible.model.response.DailyRankResponse;
import com.gntcyouthbe.bible.model.response.RankResponse;
import com.gntcyouthbe.bible.model.response.RecentRankResponse;
import com.gntcyouthbe.bible.model.response.WeeklyRankResponse;
import com.gntcyouthbe.bible.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/recent")
    public ResponseEntity<RecentRankResponse> getRecentRank() {
        return ResponseEntity.ok(rankService.getRecentRank());
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyRankResponse> getDailyRank() {
        return ResponseEntity.ok(rankService.getDailyRank());
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyRankResponse> getWeeklyRank() {
        return ResponseEntity.ok(rankService.getWeeklyRank());
    }

}
