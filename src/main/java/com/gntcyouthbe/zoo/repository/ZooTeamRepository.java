package com.gntcyouthbe.zoo.repository;

import com.gntcyouthbe.zoo.domain.ZooTeam;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZooTeamRepository extends JpaRepository<ZooTeam, Long> {

    @Query("SELECT t FROM ZooTeam t JOIN FETCH t.leader")
    List<ZooTeam> findAllWithLeader();

    // 같은 조를 바꾸는 요청(두 번 탭, 참여와 마감 동시 등)이 차례로 처리되도록 조 행을 잠근다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM ZooTeam t WHERE t.id = :teamId")
    Optional<ZooTeam> findByIdForUpdate(@Param("teamId") Long teamId);
}
