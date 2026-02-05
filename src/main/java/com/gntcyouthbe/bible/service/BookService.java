package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.ChapterVerse;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.BookListResponse.BookListItem;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse;
import com.gntcyouthbe.bible.repository.BookRepository;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.stream.Stream;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.BOOK_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final VerseRepository verseRepository;

    @Transactional(readOnly = true)
    public BookListResponse getBookList() {
        var books = Stream.of(BookName.values())
                .sorted(Comparator.comparingInt(BookName::getOrder))
                .map(book -> new BookListItem(
                        book.name(),
                        book.getDisplayName(),
                        book.getOrder()
                ))
                .toList();
        return new BookListResponse(books);
    }

    @Transactional(readOnly = true)
    public ChapterListResponse getChapterList(final BookName bookName) {
        return new ChapterListResponse(bookName);
    }

    @Transactional(readOnly = true)
    public ChapterResponse getChapterVerses(final BookName bookName, final int chapter) {
        final Book book = getBook(bookName);
        final ChapterVerse verses = getVerses(book, chapter);
        return new ChapterResponse(verses);
    }

    private Book getBook(final BookName bookName) {
        return bookRepository.findByBookName(bookName)
                .orElseThrow(() -> new EntityNotFoundException(BOOK_NOT_FOUND));
    }

    private ChapterVerse getVerses(final Book book, final int chapter) {
        return new ChapterVerse(verseRepository.findAllByBookAndChapter(book, chapter));
    }
}
