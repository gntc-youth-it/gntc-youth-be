package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.BookName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BookListResponse {

    private final List<BookListItem> books;

    public BookListResponse() {
        this.books = Stream.of(BookName.values())
                .sorted(Comparator.comparingInt(BookName::getOrder))
                .map(book -> new BookListItem(
                        book.name(),
                        book.getDisplayName(),
                        book.getOrder()
                ))
                .toList();
    }

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BookListItem {

        private final String bookCode;

        private final String bookName;

        private final int order;
    }
}
