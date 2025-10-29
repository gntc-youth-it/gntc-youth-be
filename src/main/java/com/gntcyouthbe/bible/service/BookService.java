package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.model.response.BookListResponse;
import com.gntcyouthbe.bible.model.response.RecentChapterResponse;
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
    private final VerseRepository verseRepository;
    private final VerseCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public BookListResponse getBookList(final UserPrincipal userPrincipal) {
        CellGoal goal = getCellGoal(userPrincipal);
        return new BookListResponse(goal);
    }

    private CellGoal getCellGoal(final UserPrincipal userPrincipal) {
        CellMember member = getCellMember(userPrincipal);
        Cell cell = member.getCell();
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
    public RecentChapterResponse getRecentChapter(final UserPrincipal userPrincipal) {
        Verse recentVerse = getLatestCopiedVerse(userPrincipal);
        return new RecentChapterResponse(recentVerse);
    }

    private Verse getLatestCopiedVerse(final UserPrincipal userPrincipal) {
        try {
            VerseCopy latestCopy = getLatestVerseCopy(userPrincipal);
            return latestCopy.getVerse();
        } catch (EntityNotFoundException _) {
            return getFirstVerse();
        }
    }

    private VerseCopy getLatestVerseCopy(final UserPrincipal userPrincipal) {
        return copyRepository.findFirstByUserIdOrderByCreatedAtDesc(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(VERSE_COPY_NOT_FOUND));
    }

    private Verse getFirstVerse() {
        return verseRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException(VERSE_NOT_FOUND));
    }
}
