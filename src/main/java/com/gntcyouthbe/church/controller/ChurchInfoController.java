package com.gntcyouthbe.church.controller;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.model.request.ChurchInfoRequest;
import com.gntcyouthbe.church.model.response.ChurchInfoResponse;
import com.gntcyouthbe.church.service.ChurchInfoService;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/churches/{churchId}/info")
@RequiredArgsConstructor
public class ChurchInfoController {

    private final ChurchInfoService churchInfoService;

    @PutMapping
    @PreAuthorize("hasAnyAuthority('LEADER', 'MASTER')")
    public ResponseEntity<ChurchInfoResponse> saveChurchInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable ChurchId churchId,
            @Valid @RequestBody ChurchInfoRequest request) {
        return ResponseEntity.ok(churchInfoService.saveChurchInfo(userPrincipal, churchId, request));
    }

    @GetMapping
    public ResponseEntity<ChurchInfoResponse> getChurchInfo(
            @PathVariable ChurchId churchId) {
        return ResponseEntity.ok(churchInfoService.getChurchInfo(churchId));
    }
}
