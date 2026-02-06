package com.gntcyouthbe.bible.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("BookName으로 책을 조회한다")
    void findByBookName() {
        Optional<Book> book = bookRepository.findByBookName(BookName.GENESIS);

        assertThat(book).isPresent();
        assertThat(book.get().getName()).isEqualTo("창세기");
        assertThat(book.get().getOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 BookName은 빈 Optional을 반환한다")
    void findByBookNameNotFound() {
        Optional<Book> book = bookRepository.findByBookName(BookName.EXODUS);

        assertThat(book).isEmpty();
    }
}
