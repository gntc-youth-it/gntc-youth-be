package com.gntcyouthbe.thanks.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.thanks.domain.ThanksEmoji;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksReaction;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.thanks.model.response.ThanksEntryReactionsResponse;
import com.gntcyouthbe.thanks.model.response.ThanksReactionResponse;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.thanks.repository.ThanksReactionCount;
import com.gntcyouthbe.thanks.repository.ThanksReactionRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ThanksReactionServiceTest {

    private static final Long ENTRY_ID = 12L;

    @Mock
    private ThanksReactionRepository thanksReactionRepository;

    @Mock
    private ThanksEntryRepository thanksEntryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ThanksReactionService thanksReactionService;

    private final User author = createUser(1L);
    private final User viewer = createUser(5L);
    private final ThanksEntry entry = createEntry(ENTRY_ID, author);

    // --- 켜기 ---

    @Test
    @DisplayName("반응을 누르면 감사 행을 잠근 뒤 저장하고, 그 감사의 반응 전체를 돌려준다")
    void addReaction_new() {
        // given
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));
        given(thanksReactionRepository.existsByEntryIdAndUserIdAndEmoji(ENTRY_ID, 5L, ThanksEmoji.PRAY)).willReturn(false);
        given(userRepository.findById(5L)).willReturn(Optional.of(viewer));
        given(thanksReactionRepository.countByEntryIds(List.of(ENTRY_ID)))
                .willReturn(List.of(new ThanksReactionCount(ENTRY_ID, ThanksEmoji.PRAY, 1L)));
        given(thanksReactionRepository.findByEntryIdInAndUserId(List.of(ENTRY_ID), 5L))
                .willReturn(List.of(new ThanksReaction(entry, viewer, ThanksEmoji.PRAY)));

        // when
        ThanksEntryReactionsResponse response = thanksReactionService.addReaction(principalOf(viewer), ENTRY_ID, ThanksEmoji.PRAY);

        // then
        ArgumentCaptor<ThanksReaction> saved = ArgumentCaptor.forClass(ThanksReaction.class);
        then(thanksReactionRepository).should().save(saved.capture());
        assertThat(saved.getValue().getEntry()).isEqualTo(entry);
        assertThat(saved.getValue().getUser()).isEqualTo(viewer);
        assertThat(saved.getValue().getEmoji()).isEqualTo(ThanksEmoji.PRAY);
        assertThat(response.entryId()).isEqualTo(ENTRY_ID);
        assertThat(response.reactions()).containsExactly(new ThanksReactionResponse(ThanksEmoji.PRAY, 1, true));
    }

    @Test
    @DisplayName("이미 켜진 반응을 다시 누르면 저장하지 않는다")
    void addReaction_alreadyReacted() {
        // given
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));
        given(thanksReactionRepository.existsByEntryIdAndUserIdAndEmoji(ENTRY_ID, 5L, ThanksEmoji.PRAY)).willReturn(true);

        // when
        thanksReactionService.addReaction(principalOf(viewer), ENTRY_ID, ThanksEmoji.PRAY);

        // then
        then(thanksReactionRepository).should(never()).save(any());
        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("없는 감사에는 반응을 누를 수 없다")
    void addReaction_entryNotFound() {
        // given
        given(thanksEntryRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> thanksReactionService.addReaction(principalOf(viewer), 99L, ThanksEmoji.PRAY))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ENTRY_NOT_FOUND.getCode());

        then(thanksReactionRepository).shouldHaveNoInteractions();
    }

    // --- 끄기 ---

    @Test
    @DisplayName("반응을 끄면 감사 행을 잠근 뒤 내 반응만 지우고, 남은 반응 전체를 돌려준다")
    void removeReaction() {
        // given
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));
        given(thanksReactionRepository.countByEntryIds(List.of(ENTRY_ID)))
                .willReturn(List.of(new ThanksReactionCount(ENTRY_ID, ThanksEmoji.PRAY, 1L)));

        // when
        ThanksEntryReactionsResponse response = thanksReactionService.removeReaction(principalOf(viewer), ENTRY_ID, ThanksEmoji.PRAY);

        // then
        then(thanksReactionRepository).should().deleteByEntryIdAndUserIdAndEmoji(ENTRY_ID, 5L, ThanksEmoji.PRAY);
        assertThat(response.reactions()).containsExactly(new ThanksReactionResponse(ThanksEmoji.PRAY, 1, false));
    }

    @Test
    @DisplayName("없는 감사의 반응은 끌 수 없다")
    void removeReaction_entryNotFound() {
        // given
        given(thanksEntryRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> thanksReactionService.removeReaction(principalOf(viewer), 99L, ThanksEmoji.PRAY))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ENTRY_NOT_FOUND.getCode());

        then(thanksReactionRepository).shouldHaveNoInteractions();
    }

    // --- 요약 ---

    @Test
    @DisplayName("감사별로 반응을 묶고, FE 크게 보기 순서(🙏 ❤️ 👏 🥹 😂)로 담으며, 내가 눌렀는지는 조회한 사람 기준이다")
    void summarize() {
        // given
        ThanksEntry otherEntry = createEntry(13L, author);
        given(thanksReactionRepository.countByEntryIds(List.of(ENTRY_ID, 13L, 14L))).willReturn(List.of(
                new ThanksReactionCount(ENTRY_ID, ThanksEmoji.LAUGH, 1L),
                new ThanksReactionCount(ENTRY_ID, ThanksEmoji.PRAY, 3L),
                new ThanksReactionCount(13L, ThanksEmoji.HEART, 2L)));
        given(thanksReactionRepository.findByEntryIdInAndUserId(List.of(ENTRY_ID, 13L, 14L), 5L)).willReturn(List.of(
                new ThanksReaction(entry, viewer, ThanksEmoji.PRAY),
                new ThanksReaction(otherEntry, viewer, ThanksEmoji.HEART)));

        // when
        Map<Long, List<ThanksReactionResponse>> summaries = thanksReactionService.summarize(List.of(ENTRY_ID, 13L, 14L), 5L);

        // then
        assertThat(summaries).containsOnlyKeys(ENTRY_ID, 13L);
        assertThat(summaries.get(ENTRY_ID)).containsExactly(
                new ThanksReactionResponse(ThanksEmoji.PRAY, 3, true),
                new ThanksReactionResponse(ThanksEmoji.LAUGH, 1, false));
        assertThat(summaries.get(13L)).containsExactly(new ThanksReactionResponse(ThanksEmoji.HEART, 2, true));
    }

    @Test
    @DisplayName("감사가 없으면 반응을 조회하지 않는다")
    void summarize_noEntries() {
        assertThat(thanksReactionService.summarize(List.of(), 5L)).isEmpty();
        then(thanksReactionRepository).shouldHaveNoInteractions();
    }

    private ThanksEntry createEntry(Long id, User user) {
        UploadedFile photo = new UploadedFile("thanks.webp", "stored-" + id, "uploads/stored-" + id, "image/webp", 1024L);
        ThanksEntry thanksEntry = new ThanksEntry(user, LocalDate.of(2026, 11, 9), ThanksSlot.AFTERNOON, "감사합니다", photo);
        ReflectionTestUtils.setField(thanksEntry, "id", id);
        return thanksEntry;
    }

    private UserPrincipal principalOf(User user) {
        return new UserPrincipal(user, null);
    }

    private User createUser(Long id) {
        User user = new User("user" + id + "@test.com", "사용자" + id, AuthProvider.KAKAO, "provider_" + id, Role.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
