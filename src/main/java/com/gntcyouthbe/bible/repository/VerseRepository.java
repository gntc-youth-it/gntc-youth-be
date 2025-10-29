package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.Verse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerseRepository extends JpaRepository<Verse, Long> {
}
