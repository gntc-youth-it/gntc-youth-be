package com.gntcyouthbe.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.model.request.UserNameUpdateRequest;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 이름을 정상적으로 업데이트한다")
    void updateUserNameSuccessfully() {
        User user = new User("test@example.com", "이전이름", AuthProvider.KAKAO, "12345");
        UserPrincipal principal = new UserPrincipal(1L, "test@example.com", "이전이름", Role.USER, null, AuthProvider.KAKAO);
        UserNameUpdateRequest request = new UserNameUpdateRequest("새이름");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        userService.updateUserName(principal, request);

        assertThat(user.getName()).isEqualTo("새이름");
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 이름을 변경하면 EntityNotFoundException을 던진다")
    void throwsWhenUserNotFound() {
        UserPrincipal principal = new UserPrincipal(999L, null, "테스트", Role.USER, null, null);
        UserNameUpdateRequest request = new UserNameUpdateRequest("새이름");

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserName(principal, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
