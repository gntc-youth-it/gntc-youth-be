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
import com.gntcyouthbe.user.domain.Gender;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.request.UserRoleUpdateRequest;
import com.gntcyouthbe.user.model.response.AdminUserListResponse;
import com.gntcyouthbe.user.model.response.AdminUserResponse;
import com.gntcyouthbe.user.model.response.UserRoleUpdateResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

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
    @DisplayName("사용자 목록 조회 시 userId, churchId, churchName이 포함된다")
    void getUsers_containsUserIdAndChurchId() {
        // given
        User user = createUserWithIdAndChurch(1L, "리더유저", Role.LEADER, ChurchId.ANYANG);
        UserProfile profile = new UserProfile(user, 45, "01012345678", Gender.MALE);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        given(userRepository.findAllWithChurch(pageRequest))
                .willReturn(new PageImpl<>(List.of(user), pageRequest, 1));
        given(userProfileRepository.findByUserIdIn(List.of(1L)))
                .willReturn(List.of(profile));

        // when
        AdminUserListResponse response = adminUserService.getUsers(0, 10, null);

        // then
        assertThat(response.getUsers()).hasSize(1);
        AdminUserResponse userResponse = response.getUsers().get(0);
        assertThat(userResponse.getUserId()).isEqualTo(1L);
        assertThat(userResponse.getName()).isEqualTo("리더유저");
        assertThat(userResponse.getChurchId()).isEqualTo("ANYANG");
        assertThat(userResponse.getChurchName()).isEqualTo("안양");
        assertThat(userResponse.getGeneration()).isEqualTo(45);
        assertThat(userResponse.getPhoneNumber()).isEqualTo("010-****-5678");
        assertThat(userResponse.getRole()).isEqualTo("LEADER");
    }

    @Test
    @DisplayName("성전이 없는 사용자 조회 시 churchId, churchName이 null이다")
    void getUsers_noChurch_returnsNullChurchFields() {
        // given
        User user = new User("test@example.com", "테스트유저", AuthProvider.KAKAO, "kakao_123");
        ReflectionTestUtils.setField(user, "id", 2L);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        given(userRepository.findAllWithChurch(pageRequest))
                .willReturn(new PageImpl<>(List.of(user), pageRequest, 1));
        given(userProfileRepository.findByUserIdIn(List.of(2L)))
                .willReturn(List.of());

        // when
        AdminUserListResponse response = adminUserService.getUsers(0, 10, null);

        // then
        assertThat(response.getUsers()).hasSize(1);
        AdminUserResponse userResponse = response.getUsers().get(0);
        assertThat(userResponse.getUserId()).isEqualTo(2L);
        assertThat(userResponse.getChurchId()).isNull();
        assertThat(userResponse.getChurchName()).isNull();
        assertThat(userResponse.getGeneration()).isNull();
        assertThat(userResponse.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("사용자 목록이 ID 내림차순으로 정렬되어 반환된다")
    void getUsers_sortedByIdDesc() {
        // given
        User user1 = createUserWithIdAndChurch(1L, "유저1", Role.USER, ChurchId.ANYANG);
        User user2 = createUserWithIdAndChurch(2L, "유저2", Role.USER, ChurchId.SUWON);
        User user3 = createUserWithIdAndChurch(3L, "유저3", Role.LEADER, ChurchId.ANYANG);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        given(userRepository.findAllWithChurch(pageRequest))
                .willReturn(new PageImpl<>(List.of(user3, user2, user1), pageRequest, 3));
        given(userProfileRepository.findByUserIdIn(List.of(3L, 2L, 1L)))
                .willReturn(List.of());

        // when
        AdminUserListResponse response = adminUserService.getUsers(0, 10, null);

        // then
        assertThat(response.getUsers()).hasSize(3);
        assertThat(response.getUsers().get(0).getUserId()).isEqualTo(3L);
        assertThat(response.getUsers().get(1).getUserId()).isEqualTo(2L);
        assertThat(response.getUsers().get(2).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("페이지별 조회 시 데이터가 중복 없이 분리된다")
    void getUsers_pagesDoNotOverlap() {
        // given - page 0
        User user3 = createUserWithIdAndChurch(3L, "유저3", Role.USER, ChurchId.ANYANG);
        User user2 = createUserWithIdAndChurch(2L, "유저2", Role.USER, ChurchId.SUWON);

        PageRequest page0Request = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));
        given(userRepository.findAllWithChurch(page0Request))
                .willReturn(new PageImpl<>(List.of(user3, user2), page0Request, 3));
        given(userProfileRepository.findByUserIdIn(List.of(3L, 2L)))
                .willReturn(List.of());

        // when
        AdminUserListResponse page0 = adminUserService.getUsers(0, 2, null);

        // given - page 1
        User user1 = createUserWithIdAndChurch(1L, "유저1", Role.USER, ChurchId.ANYANG);

        PageRequest page1Request = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id"));
        given(userRepository.findAllWithChurch(page1Request))
                .willReturn(new PageImpl<>(List.of(user1), page1Request, 3));
        given(userProfileRepository.findByUserIdIn(List.of(1L)))
                .willReturn(List.of());

        // when
        AdminUserListResponse page1 = adminUserService.getUsers(1, 2, null);

        // then - 페이지 간 데이터 중복 없음
        List<Long> page0Ids = page0.getUsers().stream().map(AdminUserResponse::getUserId).toList();
        List<Long> page1Ids = page1.getUsers().stream().map(AdminUserResponse::getUserId).toList();
        assertThat(page0Ids).doesNotContainAnyElementsOf(page1Ids);

        // then - 페이지 메타 정보
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page1.getUsers()).hasSize(1);
    }

    @Test
    @DisplayName("이름 검색 시에도 ID 내림차순 정렬이 적용된다")
    void getUsers_nameSearch_sortedByIdDesc() {
        // given
        User user2 = createUserWithIdAndChurch(2L, "김유저", Role.USER, ChurchId.ANYANG);
        User user1 = createUserWithIdAndChurch(1L, "김테스트", Role.USER, ChurchId.SUWON);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        given(userRepository.findAllWithChurchByNameContaining("김", pageRequest))
                .willReturn(new PageImpl<>(List.of(user2, user1), pageRequest, 2));
        given(userProfileRepository.findByUserIdIn(List.of(2L, 1L)))
                .willReturn(List.of());

        // when
        AdminUserListResponse response = adminUserService.getUsers(0, 10, "김");

        // then
        assertThat(response.getUsers()).hasSize(2);
        assertThat(response.getUsers().get(0).getUserId()).isEqualTo(2L);
        assertThat(response.getUsers().get(1).getUserId()).isEqualTo(1L);
    }

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

    private User createUserWithIdAndChurch(Long id, String name, Role role, ChurchId churchId) {
        User user = createUserWithChurch(name, role, churchId);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
