package com.gntcyouthbe.common.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.gntcyouthbe.common.security.configuration.FrontendProperties;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Mock
    private JwtService jwtService;

    @Mock
    private FrontendProperties frontendProperties;

    @Test
    @DisplayName("인증 성공 시 access token과 refresh token을 발급하고 프론트엔드로 리다이렉트한다")
    void onAuthenticationSuccess() throws Exception {
        // given
        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, AuthProvider.KAKAO);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        given(jwtService.createAccessToken(principal)).willReturn("test-access-token");
        given(jwtService.createRefreshToken(principal)).willReturn("test-refresh-token");
        given(frontendProperties.getUrl()).willReturn("https://gntc-youth.com");
        given(frontendProperties.getOauth2RedirectPath()).willReturn("/oauth2/callback");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getRedirectedUrl())
                .contains("https://gntc-youth.com/oauth2/callback")
                .contains("accessToken=test-access-token");
    }

    @Test
    @DisplayName("인증 성공 시 HttpOnly 쿠키에 refresh token을 설정한다")
    void onAuthenticationSuccess_setsRefreshCookie() throws Exception {
        // given
        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, AuthProvider.KAKAO);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        given(jwtService.createAccessToken(principal)).willReturn("test-access-token");
        given(jwtService.createRefreshToken(principal)).willReturn("test-refresh-token");
        given(frontendProperties.getUrl()).willReturn("https://gntc-youth.com");
        given(frontendProperties.getOauth2RedirectPath()).willReturn("/oauth2/callback");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        Cookie refreshCookie = response.getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("test-refresh-token");
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getSecure()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/");
        assertThat(refreshCookie.getMaxAge()).isEqualTo(60 * 60 * 24 * 14);
    }

    @Test
    @DisplayName("Set-Cookie 헤더에 SameSite=None이 포함된다")
    void onAuthenticationSuccess_sameSiteNone() throws Exception {
        // given
        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, AuthProvider.KAKAO);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        given(jwtService.createAccessToken(principal)).willReturn("access");
        given(jwtService.createRefreshToken(principal)).willReturn("refresh");
        given(frontendProperties.getUrl()).willReturn("https://gntc-youth.com");
        given(frontendProperties.getOauth2RedirectPath()).willReturn("/callback");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then — addCookie와 addHeader로 Set-Cookie가 2개 생기므로 전체 목록에서 확인
        assertThat(response.getHeaders("Set-Cookie"))
                .anyMatch(h -> h.contains("SameSite=None") && h.contains("HttpOnly") && h.contains("Secure"));
    }
}
