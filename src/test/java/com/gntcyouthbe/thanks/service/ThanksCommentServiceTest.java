package com.gntcyouthbe.thanks.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_COMMENT_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_COMMENT_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.thanks.domain.ThanksComment;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.thanks.model.request.ThanksCommentCreateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCommentListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCommentResponse;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.AuthorInfo;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.CommentSummary;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.LatestCommentInfo;
import com.gntcyouthbe.thanks.repository.ThanksCommentCount;
import com.gntcyouthbe.thanks.repository.ThanksCommentRepository;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
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
class ThanksCommentServiceTest {

    private static final Long ENTRY_ID = 12L;

    @Mock
    private ThanksCommentRepository thanksCommentRepository;

    @Mock
    private ThanksEntryRepository thanksEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private ThanksCommentService thanksCommentService;

    private final User entryAuthor = createUser(1L, Role.USER);
    private final User commenter = createUser(5L, Role.USER);
    private final User master = createUser(3L, Role.MASTER);
    private final ThanksEntry entry = createEntry(ENTRY_ID, entryAuthor);

    // --- 조회 ---

    @Test
    @DisplayName("댓글을 오래된 순 그대로 담고, 작성자 프로필 사진과 내 댓글인지를 채운다")
    void getComments() {
        // given
        ThanksComment first = createComment(1L, commenter, "기도할게요");
        ThanksComment second = createComment(2L, master, "아멘");
        given(thanksEntryRepository.existsById(ENTRY_ID)).willReturn(true);
        given(thanksCommentRepository.findByEntryIdWithUser(ENTRY_ID)).willReturn(List.of(first, second));
        given(userProfileRepository.findProfileImagePaths(List.of(5L, 3L))).willReturn(Map.of(3L, "uploads/master.jpeg"));

        // when
        ThanksCommentListResponse response = thanksCommentService.getComments(principalOf(commenter), ENTRY_ID);

        // then
        assertThat(response.entryId()).isEqualTo(ENTRY_ID);
        assertThat(response.comments())
                .extracting(ThanksCommentResponse::id, ThanksCommentResponse::content, ThanksCommentResponse::isMine,
                        ThanksCommentResponse::author)
                .containsExactly(
                        tuple(1L, "기도할게요", true, new AuthorInfo(5L, "사용자5", null)),
                        tuple(2L, "아멘", false, new AuthorInfo(3L, "사용자3", "uploads/master.jpeg")));
    }

    @Test
    @DisplayName("없는 감사의 댓글은 조회할 수 없다")
    void getComments_entryNotFound() {
        // given
        given(thanksEntryRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> thanksCommentService.getComments(principalOf(commenter), 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ENTRY_NOT_FOUND.getCode());

        then(thanksCommentRepository).shouldHaveNoInteractions();
    }

    // --- 달기 ---

    @Test
    @DisplayName("댓글을 달면 저장하고, 그 감사의 댓글 전체를 돌려준다")
    void addComment() {
        // given
        given(thanksEntryRepository.findById(ENTRY_ID)).willReturn(Optional.of(entry));
        given(userRepository.findById(5L)).willReturn(Optional.of(commenter));
        given(thanksCommentRepository.findByEntryIdWithUser(ENTRY_ID))
                .willReturn(List.of(createComment(1L, commenter, "기도할게요")));

        // when
        ThanksCommentListResponse response = thanksCommentService.addComment(
                principalOf(commenter), ENTRY_ID, new ThanksCommentCreateRequest("  기도할게요  "));

        // then
        ArgumentCaptor<ThanksComment> saved = ArgumentCaptor.forClass(ThanksComment.class);
        then(thanksCommentRepository).should().save(saved.capture());
        assertThat(saved.getValue().getEntry()).isEqualTo(entry);
        assertThat(saved.getValue().getUser()).isEqualTo(commenter);
        assertThat(saved.getValue().getContent()).isEqualTo("기도할게요");
        assertThat(response.comments()).extracting(ThanksCommentResponse::content).containsExactly("기도할게요");
    }

    @Test
    @DisplayName("없는 감사에는 댓글을 달 수 없다")
    void addComment_entryNotFound() {
        // given
        given(thanksEntryRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> thanksCommentService.addComment(
                principalOf(commenter), 99L, new ThanksCommentCreateRequest("기도할게요")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ENTRY_NOT_FOUND.getCode());

        then(thanksCommentRepository).shouldHaveNoInteractions();
    }

    // --- 지우기 ---

    @Test
    @DisplayName("쓴 사람이 지우면 지우고, 그 감사의 남은 댓글 전체를 돌려준다")
    void deleteComment_byWriter() {
        // given
        ThanksComment comment = createComment(1L, commenter, "기도할게요");
        given(thanksCommentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(thanksCommentRepository.findByEntryIdWithUser(ENTRY_ID))
                .willReturn(List.of(createComment(2L, master, "아멘")));

        // when
        ThanksCommentListResponse response = thanksCommentService.deleteComment(principalOf(commenter), 1L);

        // then
        then(thanksCommentRepository).should().delete(comment);
        assertThat(response.entryId()).isEqualTo(ENTRY_ID);
        assertThat(response.comments()).extracting(ThanksCommentResponse::content).containsExactly("아멘");
    }

    @Test
    @DisplayName("MASTER는 남의 댓글도 지울 수 있다")
    void deleteComment_byMaster() {
        // given
        ThanksComment comment = createComment(1L, commenter, "기도할게요");
        given(thanksCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        thanksCommentService.deleteComment(principalOf(master), 1L);

        // then
        then(thanksCommentRepository).should().delete(comment);
    }

    @Test
    @DisplayName("감사를 올린 사람이라도 남의 댓글은 6007로 거절하고 지우지 않는다")
    void deleteComment_byEntryAuthor() {
        // given
        given(thanksCommentRepository.findById(1L)).willReturn(Optional.of(createComment(1L, commenter, "기도할게요")));

        // when & then
        assertThatThrownBy(() -> thanksCommentService.deleteComment(principalOf(entryAuthor), 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_COMMENT_ACCESS_DENIED.getCode());

        then(thanksCommentRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("없는 댓글은 6006으로 거절한다")
    void deleteComment_notFound() {
        // given
        given(thanksCommentRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> thanksCommentService.deleteComment(principalOf(commenter), 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_COMMENT_NOT_FOUND.getCode());
    }

    // --- 목록 카드용 요약 ---

    @Test
    @DisplayName("감사별 댓글 수와 가장 최근 댓글을 묶고, 댓글이 없는 감사는 넣지 않는다")
    void summarize() {
        // given
        ThanksEntry otherEntry = createEntry(13L, entryAuthor);
        given(thanksCommentRepository.countByEntryIds(List.of(ENTRY_ID, 13L, 14L))).willReturn(List.of(
                new ThanksCommentCount(ENTRY_ID, 2L),
                new ThanksCommentCount(13L, 1L)));
        given(thanksCommentRepository.findLatestByEntryIds(List.of(ENTRY_ID, 13L, 14L))).willReturn(List.of(
                createComment(2L, master, "아멘"),
                createComment(3L, otherEntry, commenter, "축복해요")));
        given(userProfileRepository.findProfileImagePaths(List.of(3L, 5L))).willReturn(Map.of(3L, "uploads/master.jpeg"));

        // when
        Map<Long, CommentSummary> summaries = thanksCommentService.summarize(List.of(ENTRY_ID, 13L, 14L));

        // then
        assertThat(summaries).containsOnlyKeys(ENTRY_ID, 13L);
        assertThat(summaries.get(ENTRY_ID)).isEqualTo(new CommentSummary(2,
                new LatestCommentInfo(new AuthorInfo(3L, "사용자3", "uploads/master.jpeg"), "아멘")));
        assertThat(summaries.get(13L)).isEqualTo(new CommentSummary(1,
                new LatestCommentInfo(new AuthorInfo(5L, "사용자5", null), "축복해요")));
    }

    @Test
    @DisplayName("감사가 없으면 댓글을 조회하지 않는다")
    void summarize_noEntries() {
        assertThat(thanksCommentService.summarize(List.of())).isEmpty();
        then(thanksCommentRepository).shouldHaveNoInteractions();
    }

    private ThanksComment createComment(Long id, User user, String content) {
        return createComment(id, entry, user, content);
    }

    private ThanksComment createComment(Long id, ThanksEntry thanksEntry, User user, String content) {
        ThanksComment comment = new ThanksComment(thanksEntry, user, content);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
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

    private User createUser(Long id, Role role) {
        User user = new User("user" + id + "@test.com", "사용자" + id, AuthProvider.KAKAO, "provider_" + id, role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
