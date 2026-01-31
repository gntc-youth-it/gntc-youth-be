package com.gntcyouthbe.advent.service;

import com.gntcyouthbe.advent.domain.AdventPerson;
import com.gntcyouthbe.advent.domain.AdventVerse;
import com.gntcyouthbe.advent.model.response.AdventVerseListResponse;
import com.gntcyouthbe.advent.model.response.AdventVerseListResponse.AdventVerseItem;
import com.gntcyouthbe.advent.repository.AdventPersonRepository;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.model.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdventService {

    private final AdventPersonRepository adventPersonRepository;
    private final VerseRepository verseRepository;

    public AdventVerseListResponse getAdventVerses(String name, String temple, Integer batch) {
        AdventPerson person = adventPersonRepository.findByNameAndTempleAndBatch(name, temple, batch)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.ADVENT_PERSON_NOT_FOUND));

        List<AdventVerseItem> items = new ArrayList<>();
        for (AdventVerse adventVerse : person.getVerses()) {
            Verse verse = verseRepository.findByBook_BookNameAndChapterAndNumber(
                    adventVerse.getBookName(),
                    adventVerse.getChapter(),
                    adventVerse.getVerse()
            ).orElse(null);

            String content = verse != null ? verse.getContent() : "";

            items.add(new AdventVerseItem(
                    adventVerse.getSequence(),
                    adventVerse.getBookName().name(),
                    adventVerse.getBookDisplayName(),
                    adventVerse.getChapter(),
                    adventVerse.getVerse(),
                    content
            ));
        }

        return new AdventVerseListResponse(person.getName(), person.getTemple(), person.getBatch(), items);
    }
}
