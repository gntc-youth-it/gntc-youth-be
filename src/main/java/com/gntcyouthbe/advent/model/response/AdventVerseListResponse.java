package com.gntcyouthbe.advent.model.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdventVerseListResponse {

    private final String name;
    private final String temple;
    private final Integer batch;
    private final List<AdventVerseItem> verses;

    public AdventVerseListResponse(String name, String temple, Integer batch, List<AdventVerseItem> verses) {
        this.name = name;
        this.temple = temple;
        this.batch = batch;
        this.verses = verses;
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AdventVerseItem {
        private final Integer sequence;
        private final String bookName;
        private final String bookDisplayName;
        private final Integer chapter;
        private final Integer verse;
        private final String content;
        private final String reference;

        public AdventVerseItem(Integer sequence, String bookName, String bookDisplayName,
                               Integer chapter, Integer verse, String content) {
            this.sequence = sequence;
            this.bookName = bookName;
            this.bookDisplayName = bookDisplayName;
            this.chapter = chapter;
            this.verse = verse;
            this.content = content;
            this.reference = bookDisplayName + " " + chapter + ":" + verse;
        }
    }
}
