package com.gntcyouthbe.common.security.controller;

import com.gntcyouthbe.common.security.dto.TokenRefreshRequest;
import com.gntcyouthbe.common.security.dto.TokenRefreshResponse;
import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            // Refresh Token 검증 및 userId 추출
            Long userId = jwtService.getUserIdFromRefreshToken(request.getRefreshToken());

            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 새 Access Token 생성
            String newAccessToken = jwtService.createAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getChurchId(),
                    user.getProvider()
            );

            // 새 Refresh Token 생성 (Rotating Refresh Token 전략)
            // 보안 강화: 매번 새로운 Refresh Token 발급
            String newRefreshToken = jwtService.createRefreshToken(user.getId());

            TokenRefreshResponse response = TokenRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

            return ResponseEntity.ok(response);

        } catch (JwtException e) {
            // Refresh Token이 유효하지 않거나 만료됨
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            // 기타 에러
            return ResponseEntity.status(500).build();
        }
    }
}
