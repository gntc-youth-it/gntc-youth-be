package com.gntcyouthbe.user.service;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.repository.ChurchRepository;
import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Gender;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.request.UserProfilePatchRequest;
import com.gntcyouthbe.user.model.request.UserProfileRequest;
import com.gntcyouthbe.user.model.response.UserProfileResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.PROFILE_IMAGE_NOT_ATTACHABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChurchRepository churchRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    @DisplayName("프로필 조회 성공")
    void getMyProfile_success() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UserProfile profile = new UserProfile(user, 45, "010-1234-5678", Gender.MALE);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.of(profile));

        // when
        UserProfileResponse response = userProfileService.getMyProfile(principal);

        // then
        assertThat(response.getName()).isEqualTo("테스트");
        assertThat(response.getGeneration()).isEqualTo(45);
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.getGender()).isEqualTo("MALE");
        assertThat(response.getGenderDisplay()).isEqualTo("남");
    }

    @Test
    @DisplayName("프로필 조회 - 프로필 없으면 기본 정보만 반환")
    void getMyProfile_withoutProfile() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when
        UserProfileResponse response = userProfileService.getMyProfile(principal);

        // then
        assertThat(response.getName()).isEqualTo("테스트");
        assertThat(response.getGeneration()).isNull();
        assertThat(response.getPhoneNumber()).isNull();
        assertThat(response.getGender()).isNull();
    }

    @Test
    @DisplayName("프로필 저장 - 신규 생성")
    void saveProfile_create() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        Church church = mock(Church.class);
        UserProfileRequest request = new UserProfileRequest("홍길동", ChurchId.ANYANG, 45, "010-1234-5678", Gender.MALE, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(churchRepository.getReferenceById(ChurchId.ANYANG)).willReturn(church);
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userProfileRepository.save(any(UserProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserProfileResponse response = userProfileService.saveProfile(principal, request);

        // then
        assertThat(response.getName()).isEqualTo("홍길동");
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
        Church church = mock(Church.class);
        UserProfileRequest request = new UserProfileRequest("김철수", ChurchId.SUWON, 46, "010-9876-5432", Gender.FEMALE, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(churchRepository.getReferenceById(ChurchId.SUWON)).willReturn(church);
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.of(existingProfile));
        given(userProfileRepository.save(any(UserProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserProfileResponse response = userProfileService.saveProfile(principal, request);

        // then
        assertThat(response.getName()).isEqualTo("김철수");
        assertThat(response.getGeneration()).isEqualTo(46);
        assertThat(response.getPhoneNumber()).isEqualTo("010-9876-5432");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getGenderDisplay()).isEqualTo("여");
    }

    @Test
    @DisplayName("프로필 저장 - 프로필 이미지 설정")
    void saveProfile_withProfileImage() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        Church church = mock(Church.class);
        UploadedFile uploadedFile = new UploadedFile("profile.jpg", "stored.jpg", "uploads/stored.jpg", "image/jpeg", 1024L);
        org.springframework.test.util.ReflectionTestUtils.setField(uploadedFile, "id", 10L);
        UserProfileRequest request = new UserProfileRequest("홍길동", ChurchId.ANYANG, 45, "010-1234-5678", Gender.MALE, 10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(churchRepository.getReferenceById(ChurchId.ANYANG)).willReturn(church);
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(uploadedFile));
        given(userProfileRepository.save(any(UserProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserProfileResponse response = userProfileService.saveProfile(principal, request);

        // then
        assertThat(response.getProfileImageId()).isEqualTo(10L);
        assertThat(response.getProfileImagePath()).isEqualTo("uploads/stored.jpg");
    }

    @Test
    @DisplayName("프로필 저장 실패 - 존재하지 않는 이미지 ID")
    void saveProfile_invalidImageId_throwsException() {
        // given
        UserPrincipal principal = createUserPrincipal();
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        Church church = mock(Church.class);
        UserProfileRequest request = new UserProfileRequest("홍길동", ChurchId.ANYANG, 45, "010-1234-5678", Gender.MALE, 999L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(churchRepository.getReferenceById(ChurchId.ANYANG)).willReturn(church);
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.saveProfile(principal, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("프로필 저장 실패 - 사용자 없음")
    void saveProfile_userNotFound() {
        // given
        UserPrincipal principal = createUserPrincipal();
        UserProfileRequest request = new UserProfileRequest("홍길동", ChurchId.ANYANG, 45, "010-1234-5678", Gender.MALE, null);

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.saveProfile(principal, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- 이름·사진만 저장 (PATCH) ---

    @Test
    @DisplayName("프로필 행이 없는 사람이 사진만 보내면 사진만 채운 프로필을 새로 만든다")
    void updateProfile_imageOnly_withoutProfile() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UploadedFile photo = createFile(10L, "image/webp", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(photo));

        // when
        UserProfileResponse response = userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest(null, 10L));

        // then
        ArgumentCaptor<UserProfile> saved = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(saved.capture());
        assertThat(saved.getValue().getProfileImage()).isEqualTo(photo);
        assertThat(response.getName()).isEqualTo("테스트");
        assertThat(response.getProfileImageId()).isEqualTo(10L);
        assertThat(response.getProfileImagePath()).isEqualTo(photo.getFilePath());
        assertThat(response.getGeneration()).isNull();
        assertThat(response.getPhoneNumber()).isNull();
        assertThat(response.getGender()).isNull();
    }

    @Test
    @DisplayName("사진만 바꾸면 이미 입력한 기수·전화번호·성별은 그대로다")
    void updateProfile_imageOnly_keepsExistingFields() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UserProfile profile = new UserProfile(user, 45, "010-1234-5678", Gender.MALE);
        UploadedFile photo = createFile(10L, "image/jpeg", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.of(profile));
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(photo));

        // when
        UserProfileResponse response = userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest(null, 10L));

        // then
        assertThat(response.getProfileImageId()).isEqualTo(10L);
        assertThat(response.getGeneration()).isEqualTo(45);
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.getGender()).isEqualTo("MALE");
    }

    @Test
    @DisplayName("이름만 보내면 이름만 바꾸고 프로필 행은 만들지 않는다")
    void updateProfile_nameOnly() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when
        UserProfileResponse response = userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest("김하늘", null));

        // then
        assertThat(user.getName()).isEqualTo("김하늘");
        assertThat(response.getName()).isEqualTo("김하늘");
        assertThat(response.getProfileImageId()).isNull();
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(uploadedFileRepository);
    }

    @Test
    @DisplayName("이름과 사진을 함께 바꿀 수 있다")
    void updateProfile_nameAndImage() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UploadedFile photo = createFile(10L, "image/webp", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(photo));

        // when
        UserProfileResponse response = userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest("김하늘", 10L));

        // then
        assertThat(response.getName()).isEqualTo("김하늘");
        assertThat(response.getProfileImageId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("없는 파일이면 3001로 거절한다")
    void updateProfile_fileNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(
                new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123")));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest(null, 999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", FILE_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("다른 사람이 올린 파일이면 1106으로 거절하고 이름도 바꾸지 않는다")
    void updateProfile_fileUploadedByOther() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(createFile(10L, "image/webp", 2L)));

        // when & then
        assertThatThrownBy(() -> userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest("김하늘", 10L)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", PROFILE_IMAGE_NOT_ATTACHABLE.getCode());

        assertThat(user.getName()).isEqualTo("테스트");
        verify(userProfileRepository, never()).save(any());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"video/mp4", "application/pdf"})
    @DisplayName("이미지가 아닌 파일이면 1106으로 거절한다")
    void updateProfile_notImage(String contentType) {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(
                new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123")));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(createFile(10L, contentType, 1L)));

        // when & then
        assertThatThrownBy(() -> userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest(null, 10L)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", PROFILE_IMAGE_NOT_ATTACHABLE.getCode());
    }

    @Test
    @DisplayName("지금 쓰는 사진을 다시 보내면 직접 올린 파일이 아니어도 그대로 둔다")
    void updateProfile_currentImage() {
        // given
        User user = new User("test@example.com", "테스트", AuthProvider.KAKAO, "kakao_123");
        UploadedFile currentPhoto = createFile(10L, "image/jpeg", null);
        UserProfile profile = new UserProfile(user, 45, "010-1234-5678", Gender.MALE);
        profile.updateProfileImage(currentPhoto);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userProfileRepository.findByUserId(1L)).willReturn(Optional.of(profile));
        given(uploadedFileRepository.findById(10L)).willReturn(Optional.of(currentPhoto));

        // when
        UserProfileResponse response = userProfileService.updateProfile(
                createUserPrincipal(), new UserProfilePatchRequest(null, 10L));

        // then
        assertThat(response.getProfileImageId()).isEqualTo(10L);
    }

    // 업로드 기록의 createdBy는 presigned URL을 요청한 사용자다
    private UploadedFile createFile(Long id, String contentType, Long uploadedBy) {
        UploadedFile file = new UploadedFile("profile", "stored-" + id, "uploads/stored-" + id, contentType, 1024L);
        ReflectionTestUtils.setField(file, "id", id);
        ReflectionTestUtils.setField(file, "createdBy", uploadedBy);
        return file;
    }

    private UserPrincipal createUserPrincipal() {
        return new UserPrincipal(1L, "test@example.com", "테스트", Role.USER, null, AuthProvider.KAKAO, null);
    }
}
