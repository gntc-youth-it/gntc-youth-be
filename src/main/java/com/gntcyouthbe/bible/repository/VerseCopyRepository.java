package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.BookName;
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

    @Query(value = """
    select vc.*
    from verse_copy vc
    where vc.created_at = (
        select max(v2.created_at)
        from verse_copy v2
        where v2.user_id = vc.user_id
    )
      and vc.id = (
        select max(v3.id)
        from verse_copy v3
        where v3.user_id = vc.user_id
          and v3.created_at = vc.created_at
    )
    order by vc.created_at desc, vc.id desc
    limit 10
    """, nativeQuery = true)
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
        JOIN app_user u ON u.id = vc.user_id
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

    interface UserPeriodRankRow {
        Long getCount();
        Integer getRank();
        Long getTotalContributors();
    }

    @Query(value = """
        WITH agg AS (
          SELECT vc.user_id, COUNT(*) AS cnt, MAX(vc.created_at) AS last_at
          FROM verse_copy vc
          WHERE vc.created_at >= :startUtc AND vc.created_at < :endUtc
          GROUP BY vc.user_id
        ),
        ranked AS (
          SELECT user_id, cnt, last_at,
                 RANK() OVER (ORDER BY cnt DESC, last_at DESC, user_id ASC) AS rnk
          FROM agg
        )
        SELECT r.cnt   AS count,
               r.rnk   AS rank,
               (SELECT COUNT(*) FROM agg) AS totalContributors
        FROM ranked r
        WHERE r.user_id = :userId
        """, nativeQuery = true)
    Optional<UserPeriodRankRow> findUserRankBetween(@Param("userId") Long userId,
                                                    @Param("startUtc") Instant startUtc,
                                                    @Param("endUtc") Instant endUtc);

    @Query(value = """
        SELECT COUNT(DISTINCT vc.user_id)
        FROM verse_copy vc
        WHERE vc.created_at >= :startUtc AND vc.created_at < :endUtc
        """, nativeQuery = true)
    long countDistinctUsersBetween(@Param("startUtc") Instant startUtc,
                                   @Param("endUtc") Instant endUtc);

    @Query("""
        select v.book.bookName
        from Verse v
        left join VerseCopy vc
            on vc.verse = v
           and vc.user.id = :userId
        group by v.book.bookName
        having count(distinct v.id) = count(distinct vc.verse.id)
        """)
    List<BookName> findCompletedBooksByUserId(@Param("userId") Long userId);

    @Query(value = """
        select v.chapter
        from verses v
        left join verse_copy vc
            on vc.verse_id = v.id
           and vc.user_id = :userId
        where v.book_id = :bookId
        group by v.chapter
        having count(distinct v.id) = count(distinct vc.verse_id)
        order by v.chapter
        """, nativeQuery = true)
    List<Integer> findCompletedChaptersByUserAndBook(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId
    );
}
