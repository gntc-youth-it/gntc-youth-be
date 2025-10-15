package com.gntcyouthbe.common.security.service;

import com.gntcyouthbe.common.security.configuration.FrontendProperties;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final FrontendProperties frontendProperties;

    private static final String REFRESH_COOKIE = "refresh_token";
    private static final int REFRESH_COOKIE_MAX_AGE = 60 * 60 * 24 * 14;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.createAccessToken(principal);
        String refreshToken = jwtService.createRefreshToken(principal);

        // Refresh token을 HttpOnly 쿠키로 설정
        addHttpOnlyRefreshCookie(response, refreshToken);

        // 프론트엔드로 리다이렉트 (access token은 쿼리 파라미터로 전달)
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendProperties.getUrl())
                .path(frontendProperties.getOauth2RedirectPath())
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void addHttpOnlyRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                REFRESH_COOKIE + "=" + refreshToken
                        + "; Max-Age=" + REFRESH_COOKIE_MAX_AGE
                        + "; Path=/; HttpOnly; Secure; SameSite=None");
    }
}
