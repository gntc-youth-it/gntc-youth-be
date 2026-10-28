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

    @Query("""
            SELECT new com.gntcyouthbe.thanks.repository.ThanksCommentCount(c.entry.id, COUNT(c))
            FROM ThanksComment c
            WHERE c.entry.id IN :entryIds
            GROUP BY c.entry.id
            """)
    List<ThanksCommentCount> countByEntryIds(@Param("entryIds") Collection<Long> entryIds);

    // 감사별로 가장 최근(id가 가장 큰) 댓글 하나씩
    @Query("""
            SELECT c FROM ThanksComment c
            JOIN FETCH c.user
            WHERE c.id IN (
                SELECT MAX(latest.id) FROM ThanksComment latest
                WHERE latest.entry.id IN :entryIds
                GROUP BY latest.entry.id
            )
            """)
    List<ThanksComment> findLatestByEntryIds(@Param("entryIds") Collection<Long> entryIds);

    @Modifying
    @Query("DELETE FROM ThanksComment c WHERE c.entry.id = :entryId")
    void deleteByEntryId(@Param("entryId") Long entryId);
}
