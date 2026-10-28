package com.gntcyouthbe.thanks.repository;

import com.gntcyouthbe.thanks.domain.ThanksEmoji;
import com.gntcyouthbe.thanks.domain.ThanksReaction;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThanksReactionRepository extends JpaRepository<ThanksReaction, Long> {

    boolean existsByEntryIdAndUserIdAndEmoji(Long entryId, Long userId, ThanksEmoji emoji);

    List<ThanksReaction> findByEntryIdInAndUserId(Collection<Long> entryIds, Long userId);

    @Query("""
            SELECT new com.gntcyouthbe.thanks.repository.ThanksReactionCount(r.entry.id, r.emoji, COUNT(r))
            FROM ThanksReaction r
            WHERE r.entry.id IN :entryIds
            GROUP BY r.entry.id, r.emoji
            """)
    List<ThanksReactionCount> countByEntryIds(@Param("entryIds") Collection<Long> entryIds);

    @Modifying
    @Query("DELETE FROM ThanksReaction r WHERE r.entry.id = :entryId AND r.user.id = :userId AND r.emoji = :emoji")
    void deleteByEntryIdAndUserIdAndEmoji(@Param("entryId") Long entryId, @Param("userId") Long userId,
            @Param("emoji") ThanksEmoji emoji);

    @Modifying
    @Query("DELETE FROM ThanksReaction r WHERE r.entry.id = :entryId")
    void deleteByEntryId(@Param("entryId") Long entryId);
}
