package com.gntcyouthbe.user.controller;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.model.request.UserNameUpdateRequest;
import com.gntcyouthbe.user.model.response.UserNameUpdateResponse;
import com.gntcyouthbe.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/name")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserNameUpdateResponse> updateUserName(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserNameUpdateRequest request
    ) {
        userService.updateUserName(userPrincipal, request);
        return ResponseEntity.ok(UserNameUpdateResponse.success());
    }
}
