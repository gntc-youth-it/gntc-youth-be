package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerseCopyRepository extends JpaRepository<VerseCopy, Long> {

    @Query("SELECT vc FROM VerseCopy vc WHERE vc.user.id = :userId ORDER BY vc.createdAt DESC LIMIT 1")
    Optional<VerseCopy> findLatestCopiedVerse(@Param("userId") Long userId);

    List<VerseCopy> findAllByUserIdAndVerseIdIn(Long userId, List<Long> verseIds);

    long countByUserInAndVerse_SequenceBetween(Collection<User> users, int startSeq, int endSeq);
}
