package com.gntcyouthbe.bible.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse;
import com.gntcyouthbe.bible.repository.BookRepository;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private VerseRepository verseRepository;

    @Nested
    @DisplayName("getBookList")
    class GetBookList {

        @Test
        @DisplayName("66권의 성경 목록을 순서대로 반환한다")
        void returnsAll66BooksInOrder() {
            BookListResponse response = bookService.getBookList();

            assertThat(response.getBooks()).hasSize(66);
            assertThat(response.getBooks().getFirst().getBookCode()).isEqualTo("GENESIS");
            assertThat(response.getBooks().getFirst().getOrder()).isEqualTo(1);
            assertThat(response.getBooks().getLast().getBookCode()).isEqualTo("REVELATION");
            assertThat(response.getBooks().getLast().getOrder()).isEqualTo(66);
        }
    }

    @Nested
    @DisplayName("getChapterList")
    class GetChapterList {

        @Test
        @DisplayName("해당 책의 장 수를 반환한다")
        void returnsChapterCount() {
            ChapterListResponse response = bookService.getChapterList(BookName.GENESIS);

            assertThat(response.getChapters()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("getChapterVerses")
    class GetChapterVerses {

        @Test
        @DisplayName("정상적으로 구절 목록을 반환한다")
        void returnsVerses() {
            Book book = mock(Book.class);
            Verse verse = mock(Verse.class);
            given(verse.getChapter()).willReturn(1);
            given(verse.getId()).willReturn(1L);
            given(verse.getNumber()).willReturn(1);
            given(verse.getContent()).willReturn("태초에 하나님이 천지를 창조하시니라");

            given(bookRepository.findByBookName(BookName.GENESIS)).willReturn(Optional.of(book));
            given(verseRepository.findAllByBookAndChapter(book, 1)).willReturn(new ArrayList<>(List.of(verse)));

            ChapterResponse response = bookService.getChapterVerses(BookName.GENESIS, 1);

            assertThat(response.getVerses()).hasSize(1);
            assertThat(response.getVerses().getFirst().getContent()).isEqualTo("태초에 하나님이 천지를 창조하시니라");
        }

        @Test
        @DisplayName("존재하지 않는 책이면 EntityNotFoundException을 던진다")
        void throwsWhenBookNotFound() {
            given(bookRepository.findByBookName(BookName.GENESIS)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.getChapterVerses(BookName.GENESIS, 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 장이면 EntityNotFoundException을 던진다")
        void throwsWhenChapterNotFound() {
            Book book = mock(Book.class);

            given(bookRepository.findByBookName(BookName.GENESIS)).willReturn(Optional.of(book));
            given(verseRepository.findAllByBookAndChapter(book, 999)).willReturn(List.of());

            assertThatThrownBy(() -> bookService.getChapterVerses(BookName.GENESIS, 999))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
