package com.gntcyouthbe.user.controller;

import com.gntcyouthbe.user.model.response.AdminUserListResponse;
import com.gntcyouthbe.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('MASTER')")
    public ResponseEntity<AdminUserListResponse> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }
}
