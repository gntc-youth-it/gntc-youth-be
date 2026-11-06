package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanksCellMemberRepository extends JpaRepository<ThanksCellMember, Long> {

    Optional<ThanksCellMember> findByUserId(Long userId);

    // 구역 현황에서 사람마다 따로 조회하지 않도록 사용자를 함께 읽는다
    @Query("SELECT m FROM ThanksCellMember m JOIN FETCH m.user WHERE m.cell.id = :cellId")
    List<ThanksCellMember> findByCellIdWithUser(@Param("cellId") Long cellId);
}
