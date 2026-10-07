package com.gntcyouthbe.zoo.repository;

import com.gntcyouthbe.zoo.domain.ZooPhotoVote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZooPhotoVoteRepository extends JpaRepository<ZooPhotoVote, Long> {

    boolean existsByMissionIdAndUserId(Long missionId, Long userId);

    @Query("SELECT v.mission.id FROM ZooPhotoVote v WHERE v.user.id = :userId")
    List<Long> findMissionIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT v.mission.id, COUNT(v) FROM ZooPhotoVote v GROUP BY v.mission.id")
    List<Object[]> countGroupByMissionId();

    // DB에서 미션만 지우고 표를 남긴 경우에도 결과가 부풀지 않도록 남아 있는 미션의 표만 센다
    @Query("SELECT COUNT(DISTINCT v.user.id) FROM ZooPhotoVote v JOIN v.mission m")
    long countDistinctVoters();

    @Modifying
    @Query("DELETE FROM ZooPhotoVote v WHERE v.mission.id = :missionId AND v.user.id = :userId")
    void deleteByMissionIdAndUserId(@Param("missionId") Long missionId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ZooPhotoVote v WHERE v.mission.id IN (SELECT m.id FROM ZooTeamMission m WHERE m.team.id = :teamId)")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
