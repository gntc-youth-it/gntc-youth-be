package com.gntcyouthbe.bible.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ChapterVerseTest {

    @Nested
    @DisplayName("생성자 검증")
    class Constructor {

        @Test
        @DisplayName("같은 장의 구절들로 생성할 수 있다")
        void createsWithSameChapterVerses() {
            Verse verse1 = mockVerse(1L, 1, 2);
            Verse verse2 = mockVerse(2L, 1, 1);

            ChapterVerse chapterVerse = new ChapterVerse(new ArrayList<>(List.of(verse1, verse2)));

            assertThat(chapterVerse.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("다른 장의 구절이 포함되면 예외를 던진다")
        void throwsWhenDifferentChapters() {
            Verse verse1 = mockVerse(1L, 1, 1);
            Verse verse2 = mockVerse(2L, 2, 1);

            assertThatThrownBy(() -> new ChapterVerse(new ArrayList<>(List.of(verse1, verse2))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("하나의 장에 속하지 않은 구절");
        }
    }

    @Nested
    @DisplayName("sortVerses")
    class SortVerses {

        @Test
        @DisplayName("구절을 sequence 기준으로 정렬한다")
        void sortsVersesBySequence() {
            Verse verse1 = mockVerse(1L, 1, 30);
            Verse verse2 = mockVerse(2L, 1, 10);
            Verse verse3 = mockVerse(3L, 1, 20);

            ChapterVerse chapterVerse = new ChapterVerse(new ArrayList<>(List.of(verse1, verse2, verse3)));

            List<Verse> sorted = chapterVerse.getVerses();
            assertThat(sorted.get(0).getSequence()).isEqualTo(10);
            assertThat(sorted.get(1).getSequence()).isEqualTo(20);
            assertThat(sorted.get(2).getSequence()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("getVerses")
    class GetVerses {

        @Test
        @DisplayName("수정 불가능한 리스트를 반환한다")
        void returnsUnmodifiableList() {
            Verse verse = mockVerse(1L, 1, 1);

            ChapterVerse chapterVerse = new ChapterVerse(new ArrayList<>(List.of(verse)));

            assertThatThrownBy(() -> chapterVerse.getVerses().add(mock(Verse.class)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("getVerseIds")
    class GetVerseIds {

        @Test
        @DisplayName("구절 ID 목록을 반환한다")
        void returnsVerseIds() {
            Verse verse1 = mockVerse(1L, 1, 10);
            Verse verse2 = mockVerse(2L, 1, 20);

            ChapterVerse chapterVerse = new ChapterVerse(new ArrayList<>(List.of(verse1, verse2)));

            assertThat(chapterVerse.getVerseIds()).containsExactly(1L, 2L);
        }
    }

    private Verse mockVerse(Long id, int chapter, int sequence) {
        Verse verse = mock(Verse.class);
        given(verse.getId()).willReturn(id);
        given(verse.getChapter()).willReturn(chapter);
        given(verse.getSequence()).willReturn(sequence);
        return verse;
    }
}
