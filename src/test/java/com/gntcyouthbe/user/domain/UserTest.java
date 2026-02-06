package com.gntcyouthbe.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.model.request.UserNameUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("기본 생성자는 Role.USER로 설정된다")
    void defaultRoleIsUser() {
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345");

        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("역할을 지정하여 생성할 수 있다")
    void createsWithSpecifiedRole() {
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345", Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Nested
    @DisplayName("getRoleName")
    class GetRoleName {

        @Test
        @DisplayName("ROLE_ 접두사가 붙은 역할명을 반환한다")
        void returnsRoleWithPrefix() {
            User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345", Role.ADMIN);

            assertThat(user.getRoleName()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("기본 사용자는 ROLE_USER를 반환한다")
        void returnsRoleUserForDefaultUser() {
            User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345");

            assertThat(user.getRoleName()).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("getChurchId")
    class GetChurchId {

        @Test
        @DisplayName("교회가 없으면 null을 반환한다")
        void returnsNullWhenNoChurch() {
            User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "12345");

            assertThat(user.getChurchId()).isNull();
        }
    }

    @Nested
    @DisplayName("updateName")
    class UpdateName {

        @Test
        @DisplayName("이름을 변경한다")
        void updatesName() {
            User user = new User("test@example.com", "이전이름", AuthProvider.KAKAO, "12345");
            UserNameUpdateRequest request = new UserNameUpdateRequest("새이름");

            user.updateName(request);

            assertThat(user.getName()).isEqualTo("새이름");
        }
    }
}
