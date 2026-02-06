package com.gntcyouthbe.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("provider와 providerUserId로 사용자를 조회한다")
    void findByProviderAndProviderUserId() {
        Optional<User> user = userRepository.findByProviderAndProviderUserId(
                AuthProvider.KAKAO, "kakao_123456");

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 provider 조합은 빈 Optional을 반환한다")
    void findByProviderAndProviderUserIdNotFound() {
        Optional<User> user = userRepository.findByProviderAndProviderUserId(
                AuthProvider.KAKAO, "nonexistent");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void findByEmail() {
        Optional<User> user = userRepository.findByEmail("test@example.com");

        assertThat(user).isPresent();
        assertThat(user.get().getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("존재하지 않는 이메일은 빈 Optional을 반환한다")
    void findByEmailNotFound() {
        Optional<User> user = userRepository.findByEmail("nonexistent@example.com");

        assertThat(user).isEmpty();
    }
}
