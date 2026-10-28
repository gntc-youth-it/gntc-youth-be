package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanksEntryRepository extends JpaRepository<ThanksEntry, Long> {

    boolean existsByUserIdAndMissionDateAndSlot(Long userId, LocalDate missionDate, ThanksSlot slot);

    boolean existsByFileId(Long fileId);

    // 반응을 바꾸거나 감사를 지우는 요청은 이 행을 잠근 뒤 처리해서, 같은 감사에 대한 요청이 차례로 실행되게 한다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ThanksEntry e WHERE e.id = :entryId")
    Optional<ThanksEntry> findByIdForUpdate(@Param("entryId") Long entryId);

    @Query("""
            SELECT e FROM ThanksEntry e
            JOIN FETCH e.user
            JOIN FETCH e.file
            WHERE e.missionDate = :missionDate
            ORDER BY e.id
            """)
    List<ThanksEntry> findByMissionDateWithUserAndFile(@Param("missionDate") LocalDate missionDate);
}
