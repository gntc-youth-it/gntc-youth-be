package com.gntcyouthbe.bible.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChapterVerseTest {

    private Verse createMockVerse(Long id, int chapter, int number, int sequence) {
        Verse verse = mock(Verse.class);
        when(verse.getId()).thenReturn(id);
        when(verse.getChapter()).thenReturn(chapter);
        when(verse.getNumber()).thenReturn(number);
        when(verse.getSequence()).thenReturn(sequence);
        return verse;
    }

    @Test
    @DisplayName("같은 장의 절들로 ChapterVerse 생성 성공")
    void create() {
        // given
        Verse verse1 = createMockVerse(1L, 1, 1, 2);
        Verse verse2 = createMockVerse(2L, 1, 2, 1);

        // when
        ChapterVerse chapterVerse = new ChapterVerse(new ArrayList<>(List.of(verse1, verse2)));

        // then
        assertThat(chapterVerse.size()).isEqualTo(2);
        assertThat(chapterVerse.getVerses().get(0).getSequence()).isEqualTo(1);
        assertThat(chapterVerse.getVerses().get(1).getSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("다른 장의 절이 포함되면 예외 발생")
    void create_differentChapters() {
        // given
        Verse verse1 = createMockVerse(1L, 1, 1, 1);
        Verse verse2 = createMockVerse(2L, 2, 1, 2);

        // when & then
        assertThatThrownBy(() -> new ChapterVerse(new ArrayList<>(List.of(verse1, verse2))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("절 ID 목록 반환")
    void getVerseIds() {
        // given
        Verse verse1 = createMockVerse(10L, 1, 1, 1);
        Verse verse2 = createMockVerse(20L, 1, 2, 2);
        ChapterVerse chapterVerse = new ChapterVerse(new ArrayList<>(List.of(verse1, verse2)));

        // when & then
        assertThat(chapterVerse.getVerseIds()).containsExactly(10L, 20L);
    }
}
