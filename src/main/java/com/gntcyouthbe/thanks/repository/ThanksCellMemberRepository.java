package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThanksCellMemberRepository extends JpaRepository<ThanksCellMember, Long> {

    Optional<ThanksCellMember> findByUserId(Long userId);
}
