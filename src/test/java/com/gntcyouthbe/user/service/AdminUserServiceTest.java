package com.gntcyouthbe.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.repository.ChurchRepository;
import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.model.request.UserRoleUpdateRequest;
import com.gntcyouthbe.user.model.response.UserRoleUpdateResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ChurchRepository churchRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    @DisplayName("USER를 LEADER로 변경 성공 - 기존 LEADER 없음")
    void updateUserRole_toLeader_noExistingLeader() {
        // given
        User user = createUserWithChurch("테스트", Role.USER, ChurchId.SUWON);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findLeaderByChurchId(ChurchId.SUWON)).willReturn(Optional.empty());

        // when
        UserRoleUpdateResponse response = adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.LEADER));

        // then
        assertThat(response.getRole()).isEqualTo("LEADER");
        assertThat(response.getPreviousLeader()).isNull();
        assertThat(user.getRole()).isEqualTo(Role.LEADER);
    }

    @Test
    @DisplayName("USER를 LEADER로 변경 성공 - 기존 LEADER 자동 강등")
    void updateUserRole_toLeader_demoteExistingLeader() {
        // given
        User user = createUserWithChurch("신규회장", Role.USER, ChurchId.ANYANG);
        User existingLeader = createUserWithChurch("기존회장", Role.LEADER, ChurchId.ANYANG);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findLeaderByChurchId(ChurchId.ANYANG)).willReturn(Optional.of(existingLeader));

        // when
        UserRoleUpdateResponse response = adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.LEADER));

        // then
        assertThat(response.getRole()).isEqualTo("LEADER");
        assertThat(response.getPreviousLeader()).isNotNull();
        assertThat(response.getPreviousLeader().getName()).isEqualTo("기존회장");
        assertThat(response.getPreviousLeader().getRole()).isEqualTo("USER");
        assertThat(user.getRole()).isEqualTo(Role.LEADER);
        assertThat(existingLeader.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("LEADER를 USER로 변경 성공")
    void updateUserRole_toUser() {
        // given
        User user = createUserWithChurch("회장", Role.LEADER, ChurchId.ANYANG);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserRoleUpdateResponse response = adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.USER));

        // then
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getPreviousLeader()).isNull();
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 역할 변경 시 예외")
    void updateUserRole_userNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(999L, new UserRoleUpdateRequest(Role.LEADER)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("이미 같은 역할이면 예외")
    void updateUserRole_sameRole() {
        // given
        User user = createUserWithChurch("회장", Role.LEADER, ChurchId.ANYANG);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.LEADER)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("성전이 없는 사용자를 LEADER로 변경하면 예외")
    void updateUserRole_noChurch() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.LEADER)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("MASTER 역할로 변경 시도하면 예외")
    void updateUserRole_invalidRole_master() {
        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.MASTER)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("ADMIN 역할로 변경 시도하면 예외")
    void updateUserRole_invalidRole_admin() {
        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(1L, new UserRoleUpdateRequest(Role.ADMIN)))
                .isInstanceOf(BadRequestException.class);
    }

    private User createUserWithChurch(String name, Role role, ChurchId churchId) {
        Church church = mock(Church.class);
        lenient().when(church.getId()).thenReturn(churchId);

        User user = new User("test@example.com", name, AuthProvider.KAKAO, "kakao_123", role);
        user.updateNameAndChurch(name, church);
        return user;
    }
}
