package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.cell.domain.CellGoal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BookListResponse {

    private final List<BookListItem> books;

    public BookListResponse(final CellGoal goal, final Set<BookName> completedBooks) {
        this.books = Stream.of(BookName.values())
                .sorted(Comparator.comparingInt(BookName::getOrder))
                .map(book -> new BookListItem(
                        book.name(),
                        book.getDisplayName(),
                        book.getOrder(),
                        book.getOrder() >= goal.getStartBookOrder() && book.getOrder() <= goal.getEndBookOrder(),
                        completedBooks.contains(book)
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

        @JsonProperty("is_mission")
        private final boolean mission;

        @JsonProperty("is_completed")
        private final boolean completed;
    }
}
