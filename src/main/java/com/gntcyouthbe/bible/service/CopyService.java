package com.gntcyouthbe.bible.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.VERSE_COPY_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.VERSE_NOT_FOUND;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.bible.model.response.RecentChapterResponse;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CopyService {

    private final VerseRepository verseRepository;
    private final VerseCopyRepository copyRepository;
    private final UserRepository userRepository;

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

    public void copyVerse(final UserPrincipal userPrincipal, final Long verseId) {
        final User user = getUser(userPrincipal);
        final Verse verse = getVerse(verseId);
        final VerseCopy copy = verse.copy(user);
        copyRepository.save(verseCopy);
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
