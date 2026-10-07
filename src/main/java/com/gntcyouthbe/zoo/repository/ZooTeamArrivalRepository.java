package com.gntcyouthbe.zoo.repository;

import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeamArrival;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZooTeamArrivalRepository extends JpaRepository<ZooTeamArrival, Long> {

    List<ZooTeamArrival> findByTeamIdOrderByIdAsc(Long teamId);

    boolean existsByTeamIdAndStop(Long teamId, ZooStop stop);

    @Query("SELECT a.team.id, COUNT(a) FROM ZooTeamArrival a GROUP BY a.team.id")
    List<Object[]> countGroupByTeamId();

    @Modifying
    @Query("DELETE FROM ZooTeamArrival a WHERE a.team.id = :teamId AND a.stop = :stop")
    void deleteByTeamIdAndStop(@Param("teamId") Long teamId, @Param("stop") ZooStop stop);

    @Modifying
    @Query("DELETE FROM ZooTeamArrival a WHERE a.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
