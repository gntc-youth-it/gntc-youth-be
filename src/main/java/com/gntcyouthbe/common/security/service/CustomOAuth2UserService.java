package com.gntcyouthbe.common.security.service;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        AuthProvider authProvider = extractProvider(request);

        User user = getUser(authProvider, oAuth2User);

        return new UserPrincipal(user);
}

    private AuthProvider extractProvider(OAuth2UserRequest request) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        return AuthProvider.valueOf(registrationId.toUpperCase());
    }

    private User getUser(AuthProvider provider, OAuth2User oAuth2User) {
        String providerId = extractProviderId(oAuth2User, provider);
        return userRepository.findByProviderAndProviderUserId(provider, providerId)
                .orElseGet(() -> createNewUser(provider, oAuth2User));
    }

    private String extractProviderId(OAuth2User oAuth2User, AuthProvider authProvider) {
        return switch (authProvider) {
            case KAKAO -> String.valueOf(oAuth2User.getAttribute("id"));
            default -> throw new IllegalArgumentException("Unsupported auth provider: " + authProvider);
        };
    }

    private User createNewUser(AuthProvider provider, OAuth2User oAuth2User) {
        User user = new User(
                extractEmail(oAuth2User, provider),
                extractName(oAuth2User, provider),
                provider,
                extractProviderId(oAuth2User, provider)
        );
        return userRepository.save(user);
    }

    private String extractEmail(OAuth2User oAuth2User, AuthProvider authProvider) {
        return switch (authProvider) {
            case KAKAO -> {
                var kakaoAccount = (java.util.Map<String, Object>) oAuth2User.getAttribute("kakao_account");
                yield kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            }
            default -> throw new IllegalArgumentException("Unsupported auth provider: " + authProvider);
        };
    }

    private String extractName(OAuth2User oAuth2User, AuthProvider authProvider) {
        return switch (authProvider) {
            case KAKAO -> {
                var kakaoAccount = (java.util.Map<String, Object>) oAuth2User.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    var profile = (java.util.Map<String, Object>) kakaoAccount.get("profile");
                    yield profile != null ? (String) profile.get("nickname") : null;
                }
                yield null;
            }
            default -> throw new IllegalArgumentException("Unsupported auth provider: " + authProvider);
        };
    }
}
