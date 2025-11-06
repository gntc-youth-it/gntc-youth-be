package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.domain.VerseCopy;
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
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class CopyService {

    private final VerseRepository verseRepository;
    private final VerseCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final CellMemberRepository memberRepository;
    private final CellGoalRepository goalRepository;

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
            return getCellGoalStartVerse(userPrincipal);
        }
    }

    private VerseCopy getLatestVerseCopy(final UserPrincipal userPrincipal) {
        return copyRepository.findLatestCopiedVerse(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(VERSE_COPY_NOT_FOUND));
    }

    private Verse getCellGoalStartVerse(final UserPrincipal userPrincipal) {
        final Cell cell = getCellByUserPrincipal(userPrincipal);
        final CellGoal goal = getCellGoal(cell);
        return goal.getStartVerse();
    }

    private Cell getCellByUserPrincipal(final UserPrincipal userPrincipal) {
        final CellMember member = getCellMember(userPrincipal);
        return member.getCell();
    }

    private CellMember getCellMember(final UserPrincipal userPrincipal) {
        return memberRepository.findByUserId(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.CELL_MEMBER_NOT_FOUND));
    }

    private CellGoal getCellGoal(final Cell cell) {
        return goalRepository.findByCell(cell)
                .orElseThrow(() -> new EntityNotFoundException(CELL_MEMBER_NOT_FOUND));
    }

    public void copyVerse(final UserPrincipal userPrincipal, final Long verseId) {
        final User user = getUser(userPrincipal);
        final Verse verse = getVerse(verseId);
        final VerseCopy copy = verse.copy(user);
        copyRepository.save(copy);
    }

    private User getUser(final UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    private Verse getVerse(final Long verseId) {
        return verseRepository.findById(verseId)
                .orElseThrow(() -> new EntityNotFoundException(VERSE_NOT_FOUND));
    }
}
