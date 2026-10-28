package com.gntcyouthbe.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Gender;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.test.util.ReflectionTestUtils;

// findProfileImagePaths는 기본 메서드라서 실제 코드를 부르고, 쿼리 메서드만 대신 돌려준다
class UserProfileRepositoryTest {

    private final UserProfileRepository repository =
            mock(UserProfileRepository.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));

    @Test
    @DisplayName("사용자 id → 프로필 사진 경로로 바꾸고, 사진이 없는 프로필은 빠진다")
    void findProfileImagePaths() {
        // given
        UserProfile withImage = new UserProfile(createUser(1L));
        withImage.updateProfileImage(new UploadedFile("a.jpeg", "a", "uploads/a.jpeg", "image/jpeg", 1L));
        UserProfile withoutImage = new UserProfile(createUser(2L), 45, "010-1234-5678", Gender.MALE);
        doReturn(List.of(withImage, withoutImage)).when(repository).findByUserIdInWithProfileImage(List.of(1L, 2L, 3L));

        // when & then
        assertThat(repository.findProfileImagePaths(List.of(1L, 2L, 3L)))
                .containsExactlyEntriesOf(Map.of(1L, "uploads/a.jpeg"));
    }

    @Test
    @DisplayName("사용자가 없으면 조회하지 않는다")
    void findProfileImagePaths_noUsers() {
        assertThat(repository.findProfileImagePaths(List.of())).isEmpty();
        verify(repository, never()).findByUserIdInWithProfileImage(any());
    }

    private User createUser(Long id) {
        User user = new User("user" + id + "@test.com", "사용자" + id, AuthProvider.KAKAO, "provider_" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
