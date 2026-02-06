package com.gntcyouthbe.advent.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.bible.domain.BookName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdventVerseTest {

    @Test
    @DisplayName("getBookDisplayName은 한글 책 이름을 반환한다")
    void returnsBookDisplayName() {
        AdventVerse verse = AdventVerse.builder()
                .sequence(1).bookName(BookName.GENESIS).chapter(1).verse(1).build();

        assertThat(verse.getBookDisplayName()).isEqualTo("창세기");
    }

    @Test
    @DisplayName("getFullReference는 '책이름 장:절' 형식의 참조를 반환한다")
    void returnsFullReference() {
        AdventVerse verse = AdventVerse.builder()
                .sequence(1).bookName(BookName.PSALMS).chapter(23).verse(1).build();

        assertThat(verse.getFullReference()).isEqualTo("시편 23:1");
    }
}
