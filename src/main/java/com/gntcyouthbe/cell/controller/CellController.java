package com.gntcyouthbe.cell.controller;

import com.gntcyouthbe.cell.model.response.CellListResponse;
import com.gntcyouthbe.cell.service.CellService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
}
