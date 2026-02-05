package com.gntcyouthbe.bible.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class BookListResponse {

    private final List<BookListItem> books;

    @Getter
    @RequiredArgsConstructor
    public static class BookListItem {

        private final String bookCode;

        private final String bookName;

        private final int order;
    }
}
