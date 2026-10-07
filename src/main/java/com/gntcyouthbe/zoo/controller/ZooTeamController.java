package com.gntcyouthbe.zoo.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.model.request.ZooTeamCourseUpdateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamCreateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamLeaderTransferRequest;
import com.gntcyouthbe.zoo.model.response.MyZooTeamResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamDetailResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamListResponse;
import com.gntcyouthbe.zoo.service.ZooTeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zoo/teams")
@RequiredArgsConstructor
public class ZooTeamController {

    private final ZooTeamService zooTeamService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamListResponse> getTeams() {
        return ResponseEntity.ok(zooTeamService.getTeams());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MyZooTeamResponse> getMyTeam(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(zooTeamService.getMyTeam(userPrincipal));
    }

    // 초대 링크로 들어오므로 조원이 아니어도 조회할 수 있다
    @GetMapping("/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(zooTeamService.getTeam(teamId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> createTeam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ZooTeamCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zooTeamService.createTeam(userPrincipal, request));
    }

    @PostMapping("/{teamId}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> joinTeam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(zooTeamService.joinTeam(userPrincipal, teamId));
    }

    @PostMapping("/{teamId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> leaveTeam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId) {
        zooTeamService.leaveTeam(userPrincipal, teamId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTeam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId) {
        zooTeamService.deleteTeam(userPrincipal, teamId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> changeCourse(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId,
            @Valid @RequestBody ZooTeamCourseUpdateRequest request) {
        return ResponseEntity.ok(zooTeamService.changeCourse(userPrincipal, teamId, request));
    }

    @PostMapping("/{teamId}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> startTeam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(zooTeamService.startTeam(userPrincipal, teamId));
    }

    @PutMapping("/{teamId}/arrivals/{stopId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> markArrival(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId,
            @PathVariable ZooStop stopId) {
        return ResponseEntity.ok(zooTeamService.markArrival(userPrincipal, teamId, stopId));
    }

    // 프론트가 바뀐 조 상태로 화면을 바로 갱신하도록 204가 아니라 조 상세를 돌려준다
    @DeleteMapping("/{teamId}/arrivals/{stopId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> cancelArrival(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId,
            @PathVariable ZooStop stopId) {
        return ResponseEntity.ok(zooTeamService.cancelArrival(userPrincipal, teamId, stopId));
    }

    @PostMapping("/{teamId}/leader")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZooTeamDetailResponse> transferLeader(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long teamId,
            @Valid @RequestBody ZooTeamLeaderTransferRequest request) {
        return ResponseEntity.ok(zooTeamService.transferLeader(userPrincipal, teamId, request));
    }
}
