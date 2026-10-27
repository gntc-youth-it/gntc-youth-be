package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanksEntryRepository extends JpaRepository<ThanksEntry, Long> {

    boolean existsByUserIdAndMissionDateAndSlot(Long userId, LocalDate missionDate, ThanksSlot slot);

    boolean existsByFileId(Long fileId);

    @Query("""
            SELECT e FROM ThanksEntry e
            JOIN FETCH e.user
            JOIN FETCH e.file
            WHERE e.missionDate = :missionDate
            ORDER BY e.id
            """)
    List<ThanksEntry> findByMissionDateWithUserAndFile(@Param("missionDate") LocalDate missionDate);
}
