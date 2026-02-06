package com.gntcyouthbe.common.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.security.configuration.JwtProperties;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm",
                "test-issuer",
                900L,
                1209600L
        );
        jwtService = new JwtService(properties);
        jwtService.init();
    }

    @Nested
    @DisplayName("Access Token 생성")
    class CreateAccessToken {

        @Test
        @DisplayName("모든 클레임이 포함된 Access Token을 생성한다")
        void createAccessTokenWithAllClaims() {
            String token = jwtService.createAccessToken(
                    1L, "test@example.com", "테스트", Role.USER, ChurchId.ANYANG, AuthProvider.KAKAO);

            assertThat(token).isNotBlank();
            assertThat(jwtService.validate(token)).isTrue();

            Map<String, Object> claims = jwtService.getClaims(token);
            assertThat(claims.get("sub")).isEqualTo("1");
            assertThat(claims.get("name")).isEqualTo("테스트");
            assertThat(claims.get("role")).isEqualTo("USER");
            assertThat(claims.get("email")).isEqualTo("test@example.com");
            assertThat(claims.get("church")).isEqualTo("ANYANG");
            assertThat(claims.get("provider")).isEqualTo("KAKAO");
        }

        @Test
        @DisplayName("선택적 클레임이 null이면 토큰에 포함하지 않는다")
        void createAccessTokenWithoutOptionalClaims() {
            String token = jwtService.createAccessToken(
                    1L, null, "테스트", Role.USER, null, null);

            Map<String, Object> claims = jwtService.getClaims(token);
            assertThat(claims).doesNotContainKey("email");
            assertThat(claims).doesNotContainKey("church");
            assertThat(claims).doesNotContainKey("provider");
            assertThat(claims.get("name")).isEqualTo("테스트");
            assertThat(claims.get("role")).isEqualTo("USER");
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    class CreateRefreshToken {

        @Test
        @DisplayName("Refresh Token에 type=refresh 클레임이 포함된다")
        void createRefreshTokenWithTypeClaim() {
            String token = jwtService.createRefreshToken(1L);

            assertThat(jwtService.validate(token)).isTrue();

            Map<String, Object> claims = jwtService.getClaims(token);
            assertThat(claims.get("type")).isEqualTo("refresh");
            assertThat(claims.get("sub")).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class Validate {

        @Test
        @DisplayName("유효한 토큰이면 true를 반환한다")
        void validateValidToken() {
            String token = jwtService.createAccessToken(
                    1L, "test@example.com", "테스트", Role.USER, null, null);

            assertThat(jwtService.validate(token)).isTrue();
        }

        @Test
        @DisplayName("잘못된 토큰이면 false를 반환한다")
        void validateInvalidToken() {
            assertThat(jwtService.validate("invalid.token.string")).isFalse();
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰이면 false를 반환한다")
        void validateTokenWithDifferentKey() {
            JwtProperties otherProperties = new JwtProperties(
                    "different-secret-key-that-is-long-enough-for-hmac-sha256-alg",
                    "test-issuer",
                    900L,
                    1209600L
            );
            JwtService otherService = new JwtService(otherProperties);
            otherService.init();

            String token = otherService.createAccessToken(
                    1L, null, "테스트", Role.USER, null, null);

            assertThat(jwtService.validate(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("getUserIdFromRefreshToken")
    class GetUserIdFromRefreshToken {

        @Test
        @DisplayName("Refresh Token에서 userId를 추출한다")
        void getUserIdFromRefreshToken() {
            String refreshToken = jwtService.createRefreshToken(42L);

            Long userId = jwtService.getUserIdFromRefreshToken(refreshToken);

            assertThat(userId).isEqualTo(42L);
        }

        @Test
        @DisplayName("Access Token을 전달하면 JwtException이 발생한다")
        void throwsExceptionForAccessToken() {
            String accessToken = jwtService.createAccessToken(
                    1L, null, "테스트", Role.USER, null, null);

            assertThatThrownBy(() -> jwtService.getUserIdFromRefreshToken(accessToken))
                    .isInstanceOf(JwtException.class)
                    .hasMessageContaining("refresh token");
        }
    }
}
