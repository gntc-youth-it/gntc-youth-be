package com.gntcyouthbe.advent.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.bible.domain.BookName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdventPersonTest {

    @Test
    @DisplayName("addVerse로 구절을 추가하면 양방향 관계가 설정된다")
    void addVerseSetsRelationship() {
        AdventPerson person = AdventPerson.builder()
                .name("테스트").temple("안양").batch(2024).build();
        AdventVerse verse = AdventVerse.builder()
                .sequence(1).bookName(BookName.GENESIS).chapter(1).verse(1).build();

        person.addVerse(verse);

        assertThat(person.getVerses()).hasSize(1);
        assertThat(person.getVerses().getFirst()).isEqualTo(verse);
        assertThat(verse.getPerson()).isEqualTo(person);
    }

    @Test
    @DisplayName("여러 구절을 추가할 수 있다")
    void addMultipleVerses() {
        AdventPerson person = AdventPerson.builder()
                .name("테스트").temple("안양").batch(2024).build();

        person.addVerse(AdventVerse.builder()
                .sequence(1).bookName(BookName.GENESIS).chapter(1).verse(1).build());
        person.addVerse(AdventVerse.builder()
                .sequence(2).bookName(BookName.PSALMS).chapter(23).verse(1).build());

        assertThat(person.getVerses()).hasSize(2);
    }
}
