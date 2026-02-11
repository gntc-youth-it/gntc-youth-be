package com.gntcyouthbe.user.repository;

import com.gntcyouthbe.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    List<UserProfile> findByUserIdIn(List<Long> userIds);
}
