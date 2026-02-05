package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.ChapterVerse;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChapterResponse {

    private final List<VerseItem> verses;

    public ChapterResponse(final ChapterVerse chapterVerse) {
        this.verses = chapterVerse.getVerses().stream()
                .map(v -> new VerseItem(v.getId(), v.getNumber(), v.getContent()))
                .toList();
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
