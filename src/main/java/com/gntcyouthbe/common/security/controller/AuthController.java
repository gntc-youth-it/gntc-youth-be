package com.gntcyouthbe.common.security.controller;

import com.gntcyouthbe.common.security.dto.TokenRefreshResponse;
import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // HttpOnly Cookie에서 Refresh Token 추출
            String refreshToken = extractRefreshTokenFromCookie(request);
            if (refreshToken == null) {
                return ResponseEntity.status(401).build();
            }

            // Refresh Token 검증 및 userId 추출
            Long userId = jwtService.getUserIdFromRefreshToken(refreshToken);

            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 프로필 이미지 경로 조회
            String profileImagePath = userProfileRepository.findByUserId(userId)
                    .map(UserProfile::getProfileImage)
                    .map(image -> image.getFilePath())
                    .orElse(null);

            // 새 Access Token 생성
            String newAccessToken = jwtService.createAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getChurchId(),
                    user.getProvider(),
                    profileImagePath
            );

            // 새 Refresh Token 생성 (Rotating Refresh Token 전략)
            String newRefreshToken = jwtService.createRefreshToken(user.getId());

            // HttpOnly Cookie로 새 Refresh Token 설정
            setRefreshTokenCookie(response, newRefreshToken);

            // Access Token만 응답 본문에 포함
            TokenRefreshResponse tokenResponse = TokenRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .build();

            return ResponseEntity.ok(tokenResponse);

        } catch (JwtException e) {
            // Refresh Token이 유효하지 않거나 만료됨
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            // 기타 에러
            return ResponseEntity.status(500).build();
        }
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
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
