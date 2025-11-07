package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.user.domain.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerseCopyRepository extends JpaRepository<VerseCopy, Long> {

    @Query("SELECT vc FROM VerseCopy vc WHERE vc.user.id = :userId ORDER BY vc.createdAt DESC LIMIT 1")
    Optional<VerseCopy> findLatestCopiedVerse(@Param("userId") Long userId);

    List<VerseCopy> findAllByUserIdAndVerseIdIn(Long userId, List<Long> verseIds);

    @EntityGraph(attributePaths = {"user", "verse"})
    @Query("""
        select vc
        from VerseCopy vc
        where vc.createdAt = (
            select max(v2.createdAt)
            from VerseCopy v2
            where v2.user = vc.user
        )
        and vc.id = (
            select max(v3.id)
            from VerseCopy v3
            where v3.user = vc.user
              and v3.createdAt = vc.createdAt
        )
        order by vc.createdAt desc, vc.id desc
        """)
    List<VerseCopy> findLatestPerUserOrderByCreatedAtDescLimited();

    long countByUserInAndVerse_SequenceBetween(Collection<User> users, int startSeq, int endSeq);

    interface TopRow {
        Long getUserId();
        String getUserName();
        long getCopyCount();
    }

    @Query(value = """
        SELECT u.id AS userId,
               u.name AS userName,
               COUNT(*) AS copyCount
        FROM verse_copy vc
        JOIN "user" u ON u.id = vc.user_id
        WHERE vc.created_at >= :startUtc
          AND vc.created_at <  :endUtc
        GROUP BY u.id, u.name
        ORDER BY copyCount DESC, MAX(vc.created_at) DESC
        LIMIT 5
        """, nativeQuery = true)
    List<TopRow> findTop5UsersByCopiesBetween(
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc
    );
}
