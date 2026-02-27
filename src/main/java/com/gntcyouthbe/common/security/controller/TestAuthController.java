package com.gntcyouthbe.common.security.controller;

import com.gntcyouthbe.common.security.dto.TestLoginRequest;
import com.gntcyouthbe.common.security.dto.TestLoginResponse;
import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
@Profile({"dev", "local", "test"})
public class TestAuthController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<TestLoginResponse> testLogin(
            @RequestBody TestLoginRequest request,
            HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        String profileImagePath = userProfileRepository.findByUserId(user.getId())
                .map(UserProfile::getProfileImage)
                .map(image -> image.getFilePath())
                .orElse(null);

        String accessToken = jwtService.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getChurchId(),
                user.getProvider(),
                profileImagePath
        );

        String refreshToken = jwtService.createRefreshToken(user.getId());

        // HttpOnly Cookie로 Refresh Token 설정
        setRefreshTokenCookie(httpResponse, refreshToken);

        // Access Token만 응답 본문에 포함
        TestLoginResponse response = TestLoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(14 * 24 * 60 * 60); // 14일 (초 단위)
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}
