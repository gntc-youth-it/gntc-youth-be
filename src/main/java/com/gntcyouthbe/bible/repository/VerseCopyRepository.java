package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface VerseCopyRepository extends JpaRepository<VerseCopy, Long> {
    Optional<VerseCopy> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    List<Long> findIdsByUserIdAndVerseIdIn(Long userId, List<Long> verseIds);

    long countByUserInAndVerse_SequenceBetween(Collection<User> users, int startSeq, int endSeq);
}
