package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface VerseCopyRepository extends JpaRepository<VerseCopy, Long> {
    long countByUserInAndVerse_SequenceBetween(Collection<User> users, int startSeq, int endSeq);
}
