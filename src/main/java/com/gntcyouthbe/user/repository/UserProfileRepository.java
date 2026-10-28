package com.gntcyouthbe.user.repository;

import com.gntcyouthbe.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    List<UserProfile> findByUserIdIn(List<Long> userIds);

    boolean existsByProfileImageId(Long fileId);

    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.profileImage WHERE up.user.id IN :userIds")
    List<UserProfile> findByUserIdInWithProfileImage(@Param("userIds") List<Long> userIds);

    // 사용자 id → 프로필 사진 경로(uploads/...). 사진이 없는 사람은 빠지고, 사용자가 없으면 조회하지 않는다.
    default Map<Long, String> findProfileImagePaths(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return findByUserIdInWithProfileImage(List.copyOf(userIds)).stream()
                .filter(profile -> profile.getProfileImage() != null)
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        profile -> profile.getProfileImage().getFilePath()
                ));
    }
}
