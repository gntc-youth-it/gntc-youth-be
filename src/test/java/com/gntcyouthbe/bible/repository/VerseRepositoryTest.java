package com.gntcyouthbe.bible.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.Verse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class VerseRepositoryTest {

    @Autowired
    private VerseRepository verseRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("책과 장으로 구절 목록을 조회한다")
    void findAllByBookAndChapter() {
        Book genesis = bookRepository.findByBookName(BookName.GENESIS).orElseThrow();

        List<Verse> verses = verseRepository.findAllByBookAndChapter(genesis, 1);

        assertThat(verses).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 장은 빈 리스트를 반환한다")
    void findAllByBookAndChapterEmpty() {
        Book genesis = bookRepository.findByBookName(BookName.GENESIS).orElseThrow();

        List<Verse> verses = verseRepository.findAllByBookAndChapter(genesis, 999);

        assertThat(verses).isEmpty();
    }

    @Test
    @DisplayName("BookName, 장, 절로 구절을 조회한다")
    void findByBookNameAndChapterAndNumber() {
        Optional<Verse> verse = verseRepository.findByBook_BookNameAndChapterAndNumber(
                BookName.GENESIS, 1, 1);

        assertThat(verse).isPresent();
        assertThat(verse.get().getContent()).isEqualTo("태초에 하나님이 천지를 창조하시니라");
    }

    @Test
    @DisplayName("존재하지 않는 구절은 빈 Optional을 반환한다")
    void findByBookNameAndChapterAndNumberNotFound() {
        Optional<Verse> verse = verseRepository.findByBook_BookNameAndChapterAndNumber(
                BookName.GENESIS, 1, 999);

        assertThat(verse).isEmpty();
    }
}
