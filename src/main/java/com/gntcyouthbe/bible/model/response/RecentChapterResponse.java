package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.Verse;
import lombok.Getter;

@Getter
@JsonNaming
public class RecentChapterResponse {

    private final BookName book;

    private final int chapter;

    public RecentChapterResponse(final Verse verse) {
        this.book = verse.getBookName();
        this.chapter = verse.getChapter();
    }
}
