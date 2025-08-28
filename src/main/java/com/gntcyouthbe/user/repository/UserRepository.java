package com.gntcyouthbe.user.repository;

import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
