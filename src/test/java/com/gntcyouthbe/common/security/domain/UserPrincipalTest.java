package com.gntcyouthbe.common.security.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserPrincipalTest {

    @Nested
    @DisplayName("JWT claims Map으로 생성")
    class FromClaimsMap {

        @Test
        @DisplayName("모든 필드가 포함된 claims로 생성한다")
        void createsFromFullClaims() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "1");
            claims.put("email", "test@example.com");
            claims.put("name", "테스트");
            claims.put("role", "USER");
            claims.put("church", "ANYANG");
            claims.put("provider", "KAKAO");

            UserPrincipal principal = new UserPrincipal(claims);

            assertThat(principal.getUserId()).isEqualTo(1L);
            assertThat(principal.getEmail()).isEqualTo("test@example.com");
            assertThat(principal.getName()).isEqualTo("테스트");
            assertThat(principal.getRole()).isEqualTo(Role.USER);
            assertThat(principal.getChurch()).isEqualTo(ChurchId.ANYANG);
            assertThat(principal.getProvider()).isEqualTo(AuthProvider.KAKAO);
        }

        @Test
        @DisplayName("church와 provider가 없으면 null로 설정한다")
        void createsWithNullOptionalFields() {
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "1");
            claims.put("name", "테스트");
            claims.put("role", "USER");

            UserPrincipal principal = new UserPrincipal(claims);

            assertThat(principal.getChurch()).isNull();
            assertThat(principal.getProvider()).isNull();
            assertThat(principal.getEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("User 엔티티로 생성")
    class FromUserEntity {

        @Test
        @DisplayName("User 엔티티에서 UserPrincipal을 생성한다")
        void createsFromUser() {
            User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345", Role.ADMIN);

            UserPrincipal principal = new UserPrincipal(user);

            assertThat(principal.getEmail()).isEqualTo("test@example.com");
            assertThat(principal.getName()).isEqualTo("테스트");
            assertThat(principal.getRole()).isEqualTo(Role.ADMIN);
            assertThat(principal.getProvider()).isEqualTo(AuthProvider.KAKAO);
        }
    }

    @Nested
    @DisplayName("getAuthorities")
    class GetAuthorities {

        @Test
        @DisplayName("role 기반 GrantedAuthority를 반환한다")
        void returnsRoleBasedAuthority() {
            UserPrincipal principal = new UserPrincipal(
                    1L, "test@example.com", "테스트", Role.ADMIN, null, null);

            assertThat(principal.getAuthorities())
                    .hasSize(1)
                    .extracting("authority")
                    .containsExactly("ADMIN");
        }
    }
}
