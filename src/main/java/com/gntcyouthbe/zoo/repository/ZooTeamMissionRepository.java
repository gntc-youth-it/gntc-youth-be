package com.gntcyouthbe.zoo.repository;

import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZooTeamMissionRepository extends JpaRepository<ZooTeamMission, Long> {

    Optional<ZooTeamMission> findByTeamIdAndStop(Long teamId, ZooStop stop);

    // 조를 지울 때 답(@ElementCollection)까지 지우려면 엔티티로 불러와 삭제해야 한다.
    // 잠가 두면 지우는 도중에 들어온 투표가 끝난 뒤에 표를 지우므로 표가 남지 않는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ZooTeamMission> findByTeamId(Long teamId);

    // 같은 사진에 대한 투표를 차례로 처리해서, 두 번 탭해도 UNIQUE 위반 없이 끝나게 한다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM ZooTeamMission m WHERE m.id = :missionId")
    Optional<ZooTeamMission> findByIdForUpdate(@Param("missionId") Long missionId);

    @Query("SELECT m FROM ZooTeamMission m JOIN FETCH m.team JOIN FETCH m.photo ORDER BY m.id")
    List<ZooTeamMission> findAllWithTeamAndPhoto();

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
