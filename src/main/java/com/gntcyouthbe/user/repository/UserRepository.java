package com.gntcyouthbe.user.repository;

import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.church")
    List<User> findAllWithChurch();
}
