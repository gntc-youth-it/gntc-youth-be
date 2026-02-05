package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.BookName;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChapterListResponse {

    private final int chapters;

    public ChapterListResponse(final BookName bookName) {
        this.chapters = bookName.getChapters();
    }
}
