package com.gntcyouthbe.common.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.gntcyouthbe.common.security.service.JwtService;
import com.gntcyouthbe.user.domain.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, JsonMapper.shared());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 필터를 통과하고 SecurityContext는 비어있다")
    void passesWithoutAuthorizationHeader() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 인증 정보를 설정한다")
    void setsAuthenticationForValidToken() throws Exception {
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);

        given(jwtService.validate(token)).willReturn(true);
        given(jwtService.getClaims(token)).willReturn(Map.of(
                "sub", "1",
                "name", "테스트",
                "role", Role.USER.name()
        ));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰이면 401 TOKEN_EXPIRED 응답을 반환한다")
    void returns401ForExpiredToken() throws Exception {
        String token = "expired-token";
        request.addHeader("Authorization", "Bearer " + token);

        given(jwtService.validate(token)).willThrow(new ExpiredJwtException(null, null, "expired"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("TOKEN_EXPIRED");
    }

    @Test
    @DisplayName("잘못된 토큰이면 401 INVALID_TOKEN 응답을 반환한다")
    void returns401ForInvalidToken() throws Exception {
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);

        given(jwtService.validate(token)).willThrow(new JwtException("invalid"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("INVALID_TOKEN");
    }
}
