package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.Book;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.ChapterVerse;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.ChapterListResponse;
import com.gntcyouthbe.bible.model.response.ChapterResponse;
import com.gntcyouthbe.bible.model.response.RecentChapterResponse;
import com.gntcyouthbe.bible.repository.BookRepository;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellGoal;
import com.gntcyouthbe.cell.domain.CellMember;
import com.gntcyouthbe.cell.repository.CellGoalRepository;
import com.gntcyouthbe.cell.repository.CellMemberRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.model.ExceptionCode;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class BookService {

    private final CellMemberRepository memberRepository;
    private final CellGoalRepository goalRepository;
    private final BookRepository bookRepository;
    private final VerseRepository verseRepository;
    private final VerseCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public BookListResponse getBookList(final UserPrincipal userPrincipal) {
        final CellGoal goal = getCellGoal(userPrincipal);
        return new BookListResponse(goal);
    }

    private CellGoal getCellGoal(final UserPrincipal userPrincipal) {
        final CellMember member = getCellMember(userPrincipal);
        final Cell cell = member.getCell();
        return getCellGoal(cell);
    }

    private CellMember getCellMember(final UserPrincipal userPrincipal) {
        return memberRepository.findByUserId(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(CELL_MEMBER_NOT_FOUND));
    }

    private CellGoal getCellGoal(final Cell cell) {
        return goalRepository.findByCell(cell)
                .orElseThrow(() -> new EntityNotFoundException(CELL_GOAL_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ChapterListResponse getChapterList(final UserPrincipal userPrincipal, final BookName bookName) {
        final CellGoal goal = getCellGoal(userPrincipal);
        final Book book = getBook(bookName);
        final List<Integer> completedChapters = findCompletedChapters(userPrincipal, book);
        return new ChapterListResponse(goal, bookName, completedChapters);
    }

    private List<Integer> findCompletedChapters(final UserPrincipal userPrincipal, final Book book) {
        return copyRepository.findCompletedChaptersByUserAndBook(userPrincipal.getUserId(), book.getId());
    }

    @Transactional(readOnly = true)
    public ChapterResponse getChapterVerses(
            final UserPrincipal userPrincipal,
            final BookName bookName,
            final int chapter
    ) {
        final Book book = getBook(bookName);
        final ChapterVerse verses = getVerses(book, chapter);
        final CellGoal goal = getCellGoal(userPrincipal);
        final List<VerseCopy> copies = getCopiedVerses(userPrincipal, verses);
        return new ChapterResponse(verses, goal, copies);
    }

    private Book getBook(final BookName bookName) {
        return bookRepository.findByBookName(bookName)
                .orElseThrow(() -> new EntityNotFoundException(BOOK_NOT_FOUND));
    }

    private ChapterVerse getVerses(final Book book, final int chapter) {
        return new ChapterVerse(verseRepository.findAllByBookAndChapter(book, chapter));
    }

    private List<VerseCopy> getCopiedVerses(final UserPrincipal userPrincipal, final ChapterVerse verses) {
        return copyRepository.findAllByUserIdAndVerseIdIn(
                userPrincipal.getUserId(),
                verses.getVerseIds()
        );
    }
}
