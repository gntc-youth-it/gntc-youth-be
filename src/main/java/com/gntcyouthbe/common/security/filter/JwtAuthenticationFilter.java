package com.gntcyouthbe.common.security.filter;

import tools.jackson.databind.json.JsonMapper;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.common.security.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JsonMapper jsonMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = resolveBearer(request);

        if (token != null) {
            try {
                if (jwtService.validate(token)) {
                    Map<String, Object> claims = jwtService.getClaims(token);
                    UserPrincipal principal = new UserPrincipal(claims);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal, null, principal.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException e) {
                // 토큰 만료
                sendErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다.", 401);
                return;
            } catch (JwtException e) {
                // 토큰 검증 실패
                sendErrorResponse(response, "INVALID_TOKEN", "유효하지 않은 토큰입니다.", 401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearer(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) return h.substring(7);
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, String errorCode, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = Map.of(
                "error", errorCode,
                "message", message,
                "status", status
        );

        response.getWriter().write(jsonMapper.writeValueAsString(errorResponse));
    }
}
