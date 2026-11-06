package com.gntcyouthbe.thanks.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.model.request.ThanksMyCellUpdateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCellListResponse;
import com.gntcyouthbe.thanks.service.ThanksCellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 입구는 FE가 막는다. 기존 감사 API는 구역을 검사하지 않는다.
@RestController
@RequestMapping("/thanks")
@RequiredArgsConstructor
public class ThanksCellController {

    private final ThanksCellService thanksCellService;

    @GetMapping("/cells")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksCellListResponse> getCells(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(thanksCellService.getCells(userPrincipal));
    }

    // 이미 있으면 바꾸고, 같은 구역을 다시 보내도 200이다
    @PutMapping("/my-cell")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ThanksCellListResponse> updateMyCell(@AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ThanksMyCellUpdateRequest request) {
        return ResponseEntity.ok(thanksCellService.updateMyCell(userPrincipal, request));
    }
}
