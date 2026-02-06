package com.gntcyouthbe.advent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.gntcyouthbe.advent.domain.AdventPerson;
import com.gntcyouthbe.advent.domain.AdventVerse;
import com.gntcyouthbe.advent.model.response.AdventVerseListResponse;
import com.gntcyouthbe.advent.repository.AdventPersonRepository;
import com.gntcyouthbe.bible.domain.BookName;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.repository.VerseRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdventServiceTest {

    @InjectMocks
    private AdventService adventService;

    @Mock
    private AdventPersonRepository adventPersonRepository;

    @Mock
    private VerseRepository verseRepository;

    @Test
    @DisplayName("어드벤트 구절을 정상 조회하면 content가 포함된다")
    void returnsAdventVersesWithContent() {
        AdventPerson person = AdventPerson.builder()
                .name("테스트").temple("Brisbane").batch(2024).build();
        AdventVerse adventVerse = AdventVerse.builder()
                .sequence(1).bookName(BookName.GENESIS).chapter(1).verse(1).build();
        person.addVerse(adventVerse);

        Verse verse = mock(Verse.class);
        given(verse.getContent()).willReturn("태초에 하나님이 천지를 창조하시니라");

        given(adventPersonRepository.findByNameAndTempleAndBatch("테스트", "Brisbane", 2024))
                .willReturn(Optional.of(person));
        given(verseRepository.findByBook_BookNameAndChapterAndNumber(BookName.GENESIS, 1, 1))
                .willReturn(Optional.of(verse));

        AdventVerseListResponse response = adventService.getAdventVerses("테스트", "Brisbane", 2024);

        assertThat(response.getName()).isEqualTo("테스트");
        assertThat(response.getTemple()).isEqualTo("Brisbane");
        assertThat(response.getVerses()).hasSize(1);
        assertThat(response.getVerses().getFirst().getContent()).isEqualTo("태초에 하나님이 천지를 창조하시니라");
    }

    @Test
    @DisplayName("Verse가 DB에 없으면 content가 빈 문자열이다")
    void returnsEmptyContentWhenVerseNotFound() {
        AdventPerson person = AdventPerson.builder()
                .name("테스트").temple("Brisbane").batch(2024).build();
        AdventVerse adventVerse = AdventVerse.builder()
                .sequence(1).bookName(BookName.GENESIS).chapter(1).verse(1).build();
        person.addVerse(adventVerse);

        given(adventPersonRepository.findByNameAndTempleAndBatch("테스트", "Brisbane", 2024))
                .willReturn(Optional.of(person));
        given(verseRepository.findByBook_BookNameAndChapterAndNumber(BookName.GENESIS, 1, 1))
                .willReturn(Optional.empty());

        AdventVerseListResponse response = adventService.getAdventVerses("테스트", "Brisbane", 2024);

        assertThat(response.getVerses().getFirst().getContent()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 없으면 EntityNotFoundException을 던진다")
    void throwsWhenPersonNotFound() {
        given(adventPersonRepository.findByNameAndTempleAndBatch("없는사람", "Brisbane", 2024))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> adventService.getAdventVerses("없는사람", "Brisbane", 2024))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
