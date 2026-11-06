package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
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

    // 구역 현황에 쓴다. 안 올린 사람은 결과에 없다. uk_thanks_entry_user_date_slot 인덱스의 앞 열이 user_id다.
    @Query("""
            SELECT new com.gntcyouthbe.thanks.repository.ThanksDailyCount(e.user.id, e.missionDate, COUNT(e), MAX(e.createdAt))
            FROM ThanksEntry e
            WHERE e.user.id IN :userIds
            GROUP BY e.user.id, e.missionDate
            """)
    List<ThanksDailyCount> countDailyByUserIds(@Param("userIds") Collection<Long> userIds);
}
