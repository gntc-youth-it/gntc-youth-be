package com.gntcyouthbe.cell.controller;

import com.gntcyouthbe.cell.service.CellGoalService;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bible/cell-goal")
@RequiredArgsConstructor
public class CellGoalController {

    private final CellGoalService goalService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CellGoalStatResponse> getCellGoal(
            @AuthenticationPrincipal final UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(goalService.getGoalStats(userPrincipal));
    }
}
