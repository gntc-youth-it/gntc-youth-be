package com.gntcyouthbe.user.repository;

import com.gntcyouthbe.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    List<UserProfile> findByUserIdIn(List<Long> userIds);

    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.profileImage WHERE up.user.id IN :userIds")
    List<UserProfile> findByUserIdInWithProfileImage(@Param("userIds") List<Long> userIds);
}
