package com.gntcyouthbe.common.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestOperations restOperations;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository);
        customOAuth2UserService.setRestOperations(restOperations);
    }

    @Test
    @DisplayName("기존 카카오 사용자가 있으면 해당 사용자의 UserPrincipal을 반환한다")
    @SuppressWarnings("unchecked")
    void loadUser_existingKakaoUser() {
        // given
        Map<String, Object> kakaoUserInfo = Map.of(
                "id", 12345L,
                "kakao_account", Map.of(
                        "email", "test@example.com",
                        "profile", Map.of("nickname", "테스트")
                )
        );
        given(restOperations.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(ResponseEntity.ok(kakaoUserInfo));

        User existingUser = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345", Role.USER);
        given(userRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345"))
                .willReturn(Optional.of(existingUser));

        OAuth2UserRequest request = createKakaoUserRequest();

        // when
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // then
        assertThat(result).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) result;
        assertThat(principal.getEmail()).isEqualTo("test@example.com");
        assertThat(principal.getName()).isEqualTo("테스트");
        verify(userRepository).findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("새로운 카카오 사용자면 저장 후 UserPrincipal을 반환한다")
    @SuppressWarnings("unchecked")
    void loadUser_newKakaoUser() {
        // given
        Map<String, Object> kakaoUserInfo = Map.of(
                "id", 67890L,
                "kakao_account", Map.of(
                        "email", "new@example.com",
                        "profile", Map.of("nickname", "새사용자")
                )
        );
        given(restOperations.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(ResponseEntity.ok(kakaoUserInfo));

        given(userRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "67890"))
                .willReturn(Optional.empty());
        User newUser = new User("new@example.com", "새사용자", AuthProvider.KAKAO, "67890");
        given(userRepository.save(any(User.class))).willReturn(newUser);

        OAuth2UserRequest request = createKakaoUserRequest();

        // when
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // then
        assertThat(result).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) result;
        assertThat(principal.getEmail()).isEqualTo("new@example.com");
        assertThat(principal.getName()).isEqualTo("새사용자");
        verify(userRepository).findByProviderAndProviderUserId(AuthProvider.KAKAO, "67890");
        verify(userRepository).save(any(User.class));
    }

    private OAuth2UserRequest createKakaoUserRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("kakao")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientId("test-client-id")
                .build();

        Instant now = Instant.now();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "test-token", now, now.plus(1, ChronoUnit.HOURS));

        return new OAuth2UserRequest(registration, accessToken);
    }
}
