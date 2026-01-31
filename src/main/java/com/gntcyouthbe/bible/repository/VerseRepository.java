package com.gntcyouthbe.bible.repository;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.Verse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerseRepository extends JpaRepository<Verse, Long> {

    List<Verse> findAllByBookAndChapter(Book book, int chapter);

    Optional<Verse> findByBook_BookNameAndChapterAndNumber(BookName bookName, Integer chapter, Integer number);
}
