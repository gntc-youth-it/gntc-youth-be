package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksComment;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanksCommentRepository extends JpaRepository<ThanksComment, Long> {

    // 오래된 순(채팅처럼 새 댓글이 아래)
    @Query("SELECT c FROM ThanksComment c JOIN FETCH c.user WHERE c.entry.id = :entryId ORDER BY c.id")
    List<ThanksComment> findByEntryIdWithUser(@Param("entryId") Long entryId);

    // 감사별로 가장 최근(id가 가장 큰) 댓글과 그 감사의 댓글 수. 결과는 [ThanksComment, Long] 배열이다.
    // 한 쿼리라서 두 값이 같은 시점의 값이다.
    @Query("""
            SELECT c, (SELECT COUNT(other) FROM ThanksComment other WHERE other.entry = c.entry)
            FROM ThanksComment c
            JOIN FETCH c.user
            WHERE c.id IN (
                SELECT MAX(latest.id) FROM ThanksComment latest
                WHERE latest.entry.id IN :entryIds
                GROUP BY latest.entry.id
            )
            """)
    List<Object[]> findLatestWithCountByEntryIds(@Param("entryIds") Collection<Long> entryIds);

    @Modifying
    @Query("DELETE FROM ThanksComment c WHERE c.entry.id = :entryId")
    void deleteByEntryId(@Param("entryId") Long entryId);
}
