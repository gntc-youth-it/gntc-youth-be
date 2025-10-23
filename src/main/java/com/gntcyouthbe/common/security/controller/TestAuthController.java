package com.gntcyouthbe.common.security.controller;

import com.gntcyouthbe.common.security.dto.TestLoginRequest;
import com.gntcyouthbe.common.security.dto.TestLoginResponse;
import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/test")
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class TestAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<TestLoginResponse> testLogin(@RequestBody TestLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        String accessToken = jwtService.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getChurchId(),
                user.getProvider()
        );

        String refreshToken = jwtService.createRefreshToken(user.getId());

        TestLoginResponse response = TestLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }
}
