package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse;
import com.gntcyouthbe.bible.repository.BookRepository;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private VerseRepository verseRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("성경 목록 조회 - 66권 반환")
    void getBookList() {
        // when
        BookListResponse response = bookService.getBookList();

        // then
        assertThat(response.getBooks()).hasSize(66);
        assertThat(response.getBooks().get(0).getBookCode()).isEqualTo("GENESIS");
        assertThat(response.getBooks().get(0).getBookName()).isEqualTo("창세기");
        assertThat(response.getBooks().get(0).getOrder()).isEqualTo(1);
        assertThat(response.getBooks().get(65).getBookCode()).isEqualTo("REVELATION");
        assertThat(response.getBooks().get(65).getBookName()).isEqualTo("요한계시록");
    }

    @Test
    @DisplayName("특정 책의 장 수 조회")
    void getChapterList() {
        // when
        ChapterListResponse response = bookService.getChapterList(BookName.GENESIS);

        // then
        assertThat(response.getChapters()).isEqualTo(50);
    }

    @Test
    @DisplayName("특정 장의 절 목록 조회 성공")
    void getChapterVerses_success() {
        // given
        Book book = mock(Book.class);
        Verse verse1 = mock(Verse.class);
        Verse verse2 = mock(Verse.class);

        when(verse1.getId()).thenReturn(1L);
        when(verse1.getChapter()).thenReturn(1);
        when(verse1.getNumber()).thenReturn(1);
        when(verse1.getContent()).thenReturn("태초에 하나님이 천지를 창조하시니라");
        when(verse1.getSequence()).thenReturn(1);

        when(verse2.getId()).thenReturn(2L);
        when(verse2.getChapter()).thenReturn(1);
        when(verse2.getNumber()).thenReturn(2);
        when(verse2.getContent()).thenReturn("땅이 혼돈하고 공허하며");
        when(verse2.getSequence()).thenReturn(2);

        given(bookRepository.findByBookName(BookName.GENESIS)).willReturn(Optional.of(book));
        given(verseRepository.findAllByBookAndChapter(book, 1)).willReturn(new ArrayList<>(List.of(verse1, verse2)));

        // when
        ChapterResponse response = bookService.getChapterVerses(BookName.GENESIS, 1);

        // then
        assertThat(response.getVerses()).hasSize(2);
        assertThat(response.getVerses().get(0).getVerseNumber()).isEqualTo(1);
        assertThat(response.getVerses().get(0).getContent()).isEqualTo("태초에 하나님이 천지를 창조하시니라");
        assertThat(response.getVerses().get(1).getVerseNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("절 조회 실패 - 존재하지 않는 책")
    void getChapterVerses_bookNotFound() {
        // given
        given(bookRepository.findByBookName(BookName.GENESIS)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.getChapterVerses(BookName.GENESIS, 1))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("절 조회 실패 - 존재하지 않는 장")
    void getChapterVerses_chapterNotFound() {
        // given
        Book book = mock(Book.class);
        given(bookRepository.findByBookName(BookName.GENESIS)).willReturn(Optional.of(book));
        given(verseRepository.findAllByBookAndChapter(book, 999)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> bookService.getChapterVerses(BookName.GENESIS, 999))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
