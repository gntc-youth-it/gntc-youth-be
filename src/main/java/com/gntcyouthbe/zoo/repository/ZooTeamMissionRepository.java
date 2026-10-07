package com.gntcyouthbe.zoo.repository;

import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZooTeamMissionRepository extends JpaRepository<ZooTeamMission, Long> {

    Optional<ZooTeamMission> findByTeamIdAndStop(Long teamId, ZooStop stop);

    // 조를 지울 때 답(@ElementCollection)까지 지우려면 엔티티로 불러와 삭제해야 한다
    List<ZooTeamMission> findByTeamId(Long teamId);

    @Query("""
            SELECT m FROM ZooTeamMission m
            JOIN FETCH m.photo
            LEFT JOIN FETCH m.answers
            WHERE m.team.id = :teamId
            ORDER BY m.id
            """)
    List<ZooTeamMission> findByTeamIdWithPhotoAndAnswers(@Param("teamId") Long teamId);

    @Query("""
            SELECT m FROM ZooTeamMission m
            JOIN FETCH m.photo
            LEFT JOIN FETCH m.answers
            ORDER BY m.id
            """)
    List<ZooTeamMission> findAllWithPhotoAndAnswers();
}
