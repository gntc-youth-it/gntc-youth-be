package com.gntcyouthbe.common.security.service;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    private static final String REFRESH_COOKIE = "refresh_token";
    private static final int REFRESH_COOKIE_MAX_AGE = 60 * 60 * 24 * 14;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.createAccessToken(principal);
        String refreshToken = jwtService.createRefreshToken(principal);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("""
            {
              "accessToken": "%s",
              "refreshToken": "%s"
            }
            """.formatted(accessToken, refreshToken));

        addHttpOnlyRefreshCookie(response, refreshToken);
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
