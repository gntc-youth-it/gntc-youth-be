package com.gntcyouthbe.user.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Gender;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.request.UserProfileRequest;
import com.gntcyouthbe.user.model.response.UserProfileResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    @DisplayName("프로필 조회 성공")
    void getMyProfile_success() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UserProfile profile = new UserProfile(user, 45, "010-1234-5678", Gender.MALE);

        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.of(profile));

        // when
        UserProfileResponse response = userProfileService.getMyProfile(principal);

        // then
        assertThat(response.getGeneration()).isEqualTo(45);
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.getGender()).isEqualTo("MALE");
        assertThat(response.getGenderDisplay()).isEqualTo("남");
    }

    @Test
    @DisplayName("프로필 조회 실패 - 프로필 없음")
    void getMyProfile_notFound() {
        // given
        UserPrincipal principal = createUserPrincipal();
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.getMyProfile(principal))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("프로필 저장 - 신규 생성")
    void saveProfile_create() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UserProfileRequest request = new UserProfileRequest(45, "010-1234-5678", Gender.MALE);

        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.save(any(UserProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserProfileResponse response = userProfileService.saveProfile(principal, request);

        // then
        assertThat(response.getGeneration()).isEqualTo(45);
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.getGender()).isEqualTo("MALE");
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("프로필 저장 - 기존 프로필 수정")
    void saveProfile_update() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UserProfile existingProfile = new UserProfile(user, 45, "010-1234-5678", Gender.MALE);
        UserProfileRequest request = new UserProfileRequest(46, "010-9876-5432", Gender.FEMALE);

        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.of(existingProfile));
        given(userProfileRepository.save(any(UserProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserProfileResponse response = userProfileService.saveProfile(principal, request);

        // then
        assertThat(response.getGeneration()).isEqualTo(46);
        assertThat(response.getPhoneNumber()).isEqualTo("010-9876-5432");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getGenderDisplay()).isEqualTo("여");
    }

    @Test
    @DisplayName("프로필 저장 실패 - 사용자 없음")
    void saveProfile_userNotFound() {
        // given
        UserPrincipal principal = createUserPrincipal();
        UserProfileRequest request = new UserProfileRequest(45, "010-1234-5678", Gender.MALE);

        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.saveProfile(principal, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private UserPrincipal createUserPrincipal() {
        return new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, AuthProvider.KAKAO);
    }
}
