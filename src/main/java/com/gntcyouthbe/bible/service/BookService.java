package com.gntcyouthbe.bible.service;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellMember;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final VerseCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public RecentChapterResponse getRecentChapter(final UserPrincipal userPrincipal) {
        CellGoal goal =
        Verse recentVerse = getLatestCopyVerse(userPrincipal);
    }

    private Verse getLatestCopyVerse(final UserPrincipal userPrincipal) {
        Optional<VerseCopy> latestCopy = copyRepository.findFirstByUserIdOrderByCreatedAtDesc(userPrincipal.getUserId();
        if (latestCopy.isEmpty()) {

        }

    }

    private Verse getStartVerse

    private Cell getCellByUserPrincipal(final UserPrincipal userPrincipal) {
        final CellMember member = getCellMember(userPrincipal);
        return member.getCell();
    }



}
