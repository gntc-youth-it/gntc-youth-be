package com.gntcyouthbe.bible.model.response;

import com.gntcyouthbe.bible.domain.ChapterVerse;
import lombok.Getter;

import java.util.List;

@Getter
public class ChapterResponse {

    private final List<VerseItem> verses;

    public ChapterResponse(final ChapterVerse chapterVerse) {
        this.verses = chapterVerse.getVerses().stream()
                .map(v -> new VerseItem(v.getId(), v.getNumber(), v.getContent()))
                .toList();
    }

    @Getter
    public static class VerseItem {

        private final Long verseId;

        private final int verseNumber;

        private final String content;

        public VerseItem(Long verseId, int verseNumber, String content) {
            this.verseId = verseId;
            this.verseNumber = verseNumber;
            this.content = content;
        }
    }
}
