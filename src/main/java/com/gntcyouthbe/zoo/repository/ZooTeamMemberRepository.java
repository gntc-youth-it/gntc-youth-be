package com.gntcyouthbe.zoo.repository;

import com.gntcyouthbe.zoo.domain.ZooTeamMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZooTeamMemberRepository extends JpaRepository<ZooTeamMember, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    Optional<ZooTeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    @Query("SELECT m FROM ZooTeamMember m JOIN FETCH m.team WHERE m.user.id = :userId")
    Optional<ZooTeamMember> findByUserIdWithTeam(@Param("userId") Long userId);

    @Query("SELECT m.team.id FROM ZooTeamMember m WHERE m.user.id = :userId")
    Optional<Long> findTeamIdByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM ZooTeamMember m JOIN FETCH m.user WHERE m.team.id = :teamId ORDER BY m.id")
    List<ZooTeamMember> findByTeamIdWithUser(@Param("teamId") Long teamId);

    @Query("SELECT m FROM ZooTeamMember m JOIN FETCH m.user ORDER BY m.id")
    List<ZooTeamMember> findAllWithUser();

    @Query("SELECT m.team.id, COUNT(m) FROM ZooTeamMember m GROUP BY m.team.id")
    List<Object[]> countGroupByTeamId();

    @Modifying
    @Query("DELETE FROM ZooTeamMember m WHERE m.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
