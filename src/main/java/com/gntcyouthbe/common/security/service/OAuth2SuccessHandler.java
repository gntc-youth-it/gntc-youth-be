package com.gntcyouthbe.common.security.service;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var principal = (UserPrincipal) authentication.getPrincipal();

        JwtToken accessToken = jwtTokenService.createAccessToken(principal);
        JwtToken refreshToken = jwtTokenService.createRefreshToken(principal);

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter()
                .write()
    }
}
