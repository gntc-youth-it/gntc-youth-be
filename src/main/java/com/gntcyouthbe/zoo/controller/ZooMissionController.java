package com.gntcyouthbe.zoo.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.zoo.model.response.ZooMissionResultResponse;
import com.gntcyouthbe.zoo.service.ZooTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zoo/missions")
@RequiredArgsConstructor
public class ZooMissionController {

    private final ZooTeamService zooTeamService;

    // 채점용 전체 제출 내용. MASTER가 아니면 서비스에서 403(5008)으로 거절한다
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooMissionResultResponse> getMissionResults(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(zooTeamService.getMissionResults(userPrincipal));
    }
}
