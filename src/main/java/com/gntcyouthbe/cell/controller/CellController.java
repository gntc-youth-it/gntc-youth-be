package com.gntcyouthbe.cell.controller;

import com.gntcyouthbe.cell.model.response.CellJoinResponse;
import com.gntcyouthbe.cell.model.response.CellListResponse;
import com.gntcyouthbe.cell.service.CellService;
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
@RequestMapping("/cell")
@RequiredArgsConstructor
public class CellController {

    private final CellService cellService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CellListResponse> getCellList() {
        return ResponseEntity.ok(cellService.getCellList());
    }

    @PostMapping("/{cellId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CellJoinResponse> joinCell(@PathVariable Long cellId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        cellService.joinCell(cellId, userPrincipal);
        return ResponseEntity.ok(CellJoinResponse.success());
    }
}
