package com.gntcyouthbe.thanks.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ALREADY_POSTED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_FILE_NOT_ATTACHABLE;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_SLOT_CLOSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.file.service.FileStorageService;
import com.gntcyouthbe.thanks.domain.ThanksEmoji;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksMediaType;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.thanks.model.request.ThanksEntryCreateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksEntryListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.AuthorInfo;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.CommentSummary;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.LatestCommentInfo;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.MediaInfo;
import com.gntcyouthbe.thanks.model.response.ThanksReactionResponse;
import com.gntcyouthbe.thanks.repository.ThanksCommentRepository;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.thanks.repository.ThanksReactionRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ThanksEntryServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDate MISSION_DATE = LocalDate.of(2026, 11, 9);
    private static final Long FILE_ID = 31L;
    private static final Long ENTRY_ID = 12L;

    @Mock
    private ThanksEntryRepository thanksEntryRepository;

    @Mock
    private ThanksReactionRepository thanksReactionRepository;

    @Mock
    private ThanksReactionService thanksReactionService;

    @Mock
    private ThanksCommentRepository thanksCommentRepository;

    @Mock
    private ThanksCommentService thanksCommentService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private FileStorageService fileStorageService;

    private ThanksEntryService thanksEntryService;

    private final User author = createUser(1L, "김하늘", Role.USER);
    private final User other = createUser(5L, "안양유저", Role.USER);
    private final User master = createUser(3L, "운영자", Role.MASTER);

    @BeforeEach
    void setUp() {
        thanksEntryService = serviceAt("2026-11-09T12:30");
    }

    // --- 올리기 ---

    @Test
    @DisplayName("한국 시각 12:30에 올리면 그날 AFTERNOON 칸으로 저장하고 방금 만든 감사를 돌려준다")
    void createEntry_success() {
        // given
        UploadedFile photo = createFile(FILE_ID, "image/webp", author.getId());
        givenPostable(MISSION_DATE, ThanksSlot.AFTERNOON, photo);
        givenSaveAssignsId();
        given(userProfileRepository.findProfileImagePaths(List.of(1L)))
                .willReturn(Map.of(1L, "uploads/profile.jpeg"));

        // when
        ThanksEntryResponse response = thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 사 준 동기에게 감사", FILE_ID));

        // then
        assertThat(response.id()).isEqualTo(ENTRY_ID);
        assertThat(response.missionDate()).isEqualTo(MISSION_DATE);
        assertThat(response.slot()).isEqualTo(ThanksSlot.AFTERNOON);
        assertThat(response.content()).isEqualTo("점심 사 준 동기에게 감사");
        assertThat(response.media()).isEqualTo(new MediaInfo(FILE_ID, photo.getFilePath(), ThanksMediaType.IMAGE));
        assertThat(response.author()).isEqualTo(new AuthorInfo(1L, "김하늘", "uploads/profile.jpeg"));
        assertThat(response.isMine()).isTrue();
        assertThat(response.reactions()).isEmpty();
        assertThat(response.commentCount()).isZero();
        assertThat(response.latestComment()).isNull();
    }

    @Test
    @DisplayName("서버 시계가 UTC여도 한국 날짜와 칸으로 정한다")
    void createEntry_usesKoreanDateAndSlot() {
        // given: UTC 2026-11-08 21:30은 한국 시각 2026-11-09 06:30이다
        ThanksEntryService service = serviceWith(Clock.fixed(Instant.parse("2026-11-08T21:30:00Z"), ZoneOffset.UTC));
        givenPostable(MISSION_DATE, ThanksSlot.MORNING, createFile(FILE_ID, "image/webp", author.getId()));
        givenSaveAssignsId();

        // when
        ThanksEntryResponse response = service.createEntry(principalOf(author), new ThanksEntryCreateRequest("아침 감사", FILE_ID));

        // then
        assertThat(response.missionDate()).isEqualTo(MISSION_DATE);
        assertThat(response.slot()).isEqualTo(ThanksSlot.MORNING);
    }

    @ParameterizedTest(name = "한국 시각 {0}")
    @ValueSource(strings = {"2026-11-10T00:00", "2026-11-09T05:59:59"})
    @DisplayName("한국 시각 00:00–05:59에는 열린 칸이 없어 6001로 거절한다")
    void createEntry_slotClosed(String koreanDateTime) {
        // when & then
        assertThatThrownBy(() -> serviceAt(koreanDateTime).createEntry(
                principalOf(author), new ThanksEntryCreateRequest("감사합니다", FILE_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_SLOT_CLOSED.getCode());

        then(thanksEntryRepository).shouldHaveNoInteractions();
        then(uploadedFileRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("지금 칸에 이미 올렸으면 6002로 거절하고 파일은 보지 않는다")
    void createEntry_alreadyPosted() {
        // given
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(1L, MISSION_DATE, ThanksSlot.AFTERNOON))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("또 올리는 감사", FILE_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ALREADY_POSTED.getCode());

        then(uploadedFileRepository).shouldHaveNoInteractions();
        then(thanksEntryRepository).should(never()).saveAndFlush(any());
    }

    // 같은 파일로 두 번 누르면 두 제약에 함께 걸리고, DB는 그중 하나만 알려 준다
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"uk_thanks_entry_user_date_slot", "uk_thanks_entry_file_id"})
    @DisplayName("같은 칸에 동시에 올려 unique 제약에 걸리면 어느 제약이든 6002로 바꾼다")
    void createEntry_concurrentRequest_uniqueViolation(String constraintName) {
        // given
        givenPostable(MISSION_DATE, ThanksSlot.AFTERNOON, createFile(FILE_ID, "image/webp", author.getId()));
        given(thanksEntryRepository.saveAndFlush(any(ThanksEntry.class)))
                .willThrow(integrityViolation(ConstraintKind.UNIQUE, constraintName, "23505"));

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", FILE_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ALREADY_POSTED.getCode());
    }

    @Test
    @DisplayName("unique 위반이 아닌 무결성 오류는 6002로 바꾸지 않고 그대로 던진다")
    void createEntry_otherIntegrityViolation() {
        // given
        givenPostable(MISSION_DATE, ThanksSlot.AFTERNOON, createFile(FILE_ID, "image/webp", author.getId()));
        DataIntegrityViolationException notNullViolation = integrityViolation(ConstraintKind.NOT_NULL, "content", "23502");
        given(thanksEntryRepository.saveAndFlush(any(ThanksEntry.class))).willThrow(notNullViolation);

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", FILE_ID)))
                .isSameAs(notNullViolation);
    }

    @Test
    @DisplayName("파일 id를 보내지 않으면 3001로 거절한다")
    void createEntry_withoutFileId() {
        // given
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(1L, MISSION_DATE, ThanksSlot.AFTERNOON))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", null)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", FILE_NOT_FOUND.getCode());

        then(uploadedFileRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("없는 파일이면 3001로 거절한다")
    void createEntry_fileNotFound() {
        // given
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(1L, MISSION_DATE, ThanksSlot.AFTERNOON))
                .willReturn(false);
        given(uploadedFileRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", 99L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", FILE_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("다른 사람이 올린 파일은 6005로 거절한다")
    void createEntry_fileUploadedByOther() {
        // given
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(1L, MISSION_DATE, ThanksSlot.AFTERNOON))
                .willReturn(false);
        given(uploadedFileRepository.findById(FILE_ID))
                .willReturn(Optional.of(createFile(FILE_ID, "image/webp", other.getId())));

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", FILE_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_FILE_NOT_ATTACHABLE.getCode());

        then(thanksEntryRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("사진이나 영상이 아닌 파일은 6005로 거절한다")
    void createEntry_notMediaFile() {
        // given
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(1L, MISSION_DATE, ThanksSlot.AFTERNOON))
                .willReturn(false);
        given(uploadedFileRepository.findById(FILE_ID))
                .willReturn(Optional.of(createFile(FILE_ID, "application/pdf", author.getId())));

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", FILE_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_FILE_NOT_ATTACHABLE.getCode());

        then(thanksEntryRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("이미 다른 감사에 붙은 파일은 6005로 거절한다")
    void createEntry_fileAlreadyAttached() {
        // given
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(1L, MISSION_DATE, ThanksSlot.AFTERNOON))
                .willReturn(false);
        given(uploadedFileRepository.findById(FILE_ID))
                .willReturn(Optional.of(createFile(FILE_ID, "image/webp", author.getId())));
        given(thanksEntryRepository.existsByFileId(FILE_ID)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("점심 감사", FILE_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_FILE_NOT_ATTACHABLE.getCode());

        then(thanksEntryRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("영상 파일을 붙이면 media.type은 VIDEO다")
    void createEntry_video() {
        // given
        givenPostable(MISSION_DATE, ThanksSlot.AFTERNOON, createFile(FILE_ID, "video/mp4", author.getId()));
        givenSaveAssignsId();

        // when
        ThanksEntryResponse response = thanksEntryService.createEntry(
                principalOf(author), new ThanksEntryCreateRequest("산책 감사", FILE_ID));

        // then
        assertThat(response.media().type()).isEqualTo(ThanksMediaType.VIDEO);
    }

    // --- 삭제 ---

    @Test
    @DisplayName("올린 사람이 지우면 반응과 댓글을 먼저 지우고, 감사와 함께 파일 기록과 S3 파일도 지운다")
    void deleteEntry_byAuthor() {
        // given
        UploadedFile photo = createFile(FILE_ID, "image/webp", author.getId());
        ThanksEntry entry = createEntry(ENTRY_ID, author, ThanksSlot.AFTERNOON, photo);
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));

        // when
        thanksEntryService.deleteEntry(principalOf(author), ENTRY_ID);

        // then
        InOrder inOrder = inOrder(thanksReactionRepository, thanksCommentRepository, thanksEntryRepository);
        inOrder.verify(thanksReactionRepository).deleteByEntryId(ENTRY_ID);
        inOrder.verify(thanksCommentRepository).deleteByEntryId(ENTRY_ID);
        inOrder.verify(thanksEntryRepository).delete(entry);
        then(uploadedFileRepository).should().delete(photo);
        then(fileStorageService).should().deleteFiles(List.of(photo.getFilePath()));
    }

    @Test
    @DisplayName("붙인 파일을 프로필 사진으로도 쓰고 있으면 감사만 지우고 파일 기록과 S3 파일은 남긴다")
    void deleteEntry_fileUsedAsProfileImage() {
        // given
        UploadedFile photo = createFile(FILE_ID, "image/webp", author.getId());
        ThanksEntry entry = createEntry(ENTRY_ID, author, ThanksSlot.AFTERNOON, photo);
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));
        given(userProfileRepository.existsByProfileImageId(FILE_ID)).willReturn(true);

        // when
        thanksEntryService.deleteEntry(principalOf(author), ENTRY_ID);

        // then
        then(thanksEntryRepository).should().delete(entry);
        then(uploadedFileRepository).shouldHaveNoInteractions();
        then(fileStorageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("MASTER는 남이 올린 감사도 지울 수 있다")
    void deleteEntry_byMaster() {
        // given
        UploadedFile photo = createFile(FILE_ID, "image/webp", author.getId());
        ThanksEntry entry = createEntry(ENTRY_ID, author, ThanksSlot.AFTERNOON, photo);
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));

        // when
        thanksEntryService.deleteEntry(principalOf(master), ENTRY_ID);

        // then
        then(thanksEntryRepository).should().delete(entry);
        then(uploadedFileRepository).should().delete(photo);
        then(fileStorageService).should().deleteFiles(List.of(photo.getFilePath()));
    }

    @Test
    @DisplayName("올린 사람도 MASTER도 아니면 6004로 거절하고 아무것도 지우지 않는다")
    void deleteEntry_byOther() {
        // given
        ThanksEntry entry = createEntry(ENTRY_ID, author, ThanksSlot.AFTERNOON,
                createFile(FILE_ID, "image/webp", author.getId()));
        given(thanksEntryRepository.findByIdForUpdate(ENTRY_ID)).willReturn(Optional.of(entry));

        // when & then
        assertThatThrownBy(() -> thanksEntryService.deleteEntry(principalOf(other), ENTRY_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ENTRY_ACCESS_DENIED.getCode());

        then(thanksEntryRepository).should(never()).delete(any());
        then(thanksReactionRepository).shouldHaveNoInteractions();
        then(thanksCommentRepository).shouldHaveNoInteractions();
        then(uploadedFileRepository).shouldHaveNoInteractions();
        then(fileStorageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("없는 감사는 6003으로 거절한다")
    void deleteEntry_notFound() {
        // given
        given(thanksEntryRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> thanksEntryService.deleteEntry(principalOf(author), 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", THANKS_ENTRY_NOT_FOUND.getCode());

        then(fileStorageService).shouldHaveNoInteractions();
    }

    // --- 날짜별 목록 ---

    @Test
    @DisplayName("목록에 내 감사인지와 작성자 프로필 사진을 채우고, 프로필은 작성자를 모아 한 번에 조회한다")
    void getEntries_marksMineAndProfileImages() {
        // given
        ThanksEntry authorMorning = createEntry(1L, author, ThanksSlot.MORNING, createFile(31L, "image/webp", 1L));
        ThanksEntry otherMorning = createEntry(2L, other, ThanksSlot.MORNING, createFile(32L, "image/jpeg", 5L));
        ThanksEntry authorAfternoon = createEntry(3L, author, ThanksSlot.AFTERNOON, createFile(33L, "image/webp", 1L));
        given(thanksEntryRepository.findByMissionDateWithUserAndFile(MISSION_DATE))
                .willReturn(List.of(authorMorning, otherMorning, authorAfternoon));
        given(userProfileRepository.findProfileImagePaths(List.of(1L, 5L)))
                .willReturn(Map.of(1L, "uploads/profile.jpeg"));

        // when
        ThanksEntryListResponse response = thanksEntryService.getEntries(principalOf(other), MISSION_DATE);

        // then
        assertThat(response.date()).isEqualTo(MISSION_DATE);
        assertThat(response.entries())
                .extracting(
                        ThanksEntryResponse::id,
                        ThanksEntryResponse::isMine,
                        (ThanksEntryResponse entry) -> entry.author().profileImageUrl())
                .containsExactly(
                        tuple(1L, false, "uploads/profile.jpeg"),
                        tuple(2L, true, null),
                        tuple(3L, false, "uploads/profile.jpeg"));
    }

    @Test
    @DisplayName("목록의 반응은 감사별 반응 요약으로 채우고, 반응이 없는 감사는 빈 배열이다")
    void getEntries_fillsReactions() {
        // given
        ThanksEntry reacted = createEntry(1L, author, ThanksSlot.MORNING, createFile(31L, "image/webp", 1L));
        ThanksEntry notReacted = createEntry(2L, other, ThanksSlot.MORNING, createFile(32L, "image/webp", 5L));
        List<ThanksReactionResponse> reactions = List.of(
                new ThanksReactionResponse(ThanksEmoji.PRAY, 2, true),
                new ThanksReactionResponse(ThanksEmoji.HEART, 1, false));
        given(thanksEntryRepository.findByMissionDateWithUserAndFile(MISSION_DATE)).willReturn(List.of(reacted, notReacted));
        given(thanksReactionService.summarize(List.of(1L, 2L), 5L)).willReturn(Map.of(1L, reactions));

        // when
        ThanksEntryListResponse response = thanksEntryService.getEntries(principalOf(other), MISSION_DATE);

        // then
        assertThat(response.entries())
                .extracting(ThanksEntryResponse::id, ThanksEntryResponse::reactions)
                .containsExactly(tuple(1L, reactions), tuple(2L, List.of()));
    }

    @Test
    @DisplayName("목록의 댓글 수와 최근 댓글은 감사별 댓글 요약으로 채우고, 댓글이 없는 감사는 0과 null이다")
    void getEntries_fillsComments() {
        // given
        ThanksEntry commented = createEntry(1L, author, ThanksSlot.MORNING, createFile(31L, "image/webp", 1L));
        ThanksEntry notCommented = createEntry(2L, other, ThanksSlot.MORNING, createFile(32L, "image/webp", 5L));
        LatestCommentInfo latest = new LatestCommentInfo(new AuthorInfo(5L, "안양유저", null), "기도할게요");
        given(thanksEntryRepository.findByMissionDateWithUserAndFile(MISSION_DATE)).willReturn(List.of(commented, notCommented));
        given(thanksCommentService.summarize(List.of(1L, 2L))).willReturn(Map.of(1L, new CommentSummary(2, latest)));

        // when
        ThanksEntryListResponse response = thanksEntryService.getEntries(principalOf(other), MISSION_DATE);

        // then
        assertThat(response.entries())
                .extracting(ThanksEntryResponse::id, ThanksEntryResponse::commentCount, ThanksEntryResponse::latestComment)
                .containsExactly(tuple(1L, 2L, latest), tuple(2L, 0L, null));
    }

    @Test
    @DisplayName("아무도 올리지 않은 날짜는 빈 목록이고 프로필·반응·댓글을 조회하지 않는다")
    void getEntries_empty() {
        // given
        given(thanksEntryRepository.findByMissionDateWithUserAndFile(MISSION_DATE)).willReturn(List.of());

        // when
        ThanksEntryListResponse response = thanksEntryService.getEntries(principalOf(author), MISSION_DATE);

        // then
        assertThat(response.date()).isEqualTo(MISSION_DATE);
        assertThat(response.entries()).isEmpty();
        then(userProfileRepository).shouldHaveNoInteractions();
        then(thanksReactionService).shouldHaveNoInteractions();
        then(thanksCommentService).shouldHaveNoInteractions();
    }

    // 지금 칸이 비어 있고, 작성자가 올린 사진이나 영상이라 저장까지 가는 경우
    private void givenPostable(LocalDate missionDate, ThanksSlot slot, UploadedFile file) {
        given(thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(author.getId(), missionDate, slot))
                .willReturn(false);
        given(uploadedFileRepository.findById(file.getId())).willReturn(Optional.of(file));
        given(thanksEntryRepository.existsByFileId(file.getId())).willReturn(false);
        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
    }

    // 실제 DB에서도 Spring이 Hibernate의 ConstraintViolationException을 이렇게 감싸서 던진다(ThanksEntryRepositoryTest)
    private DataIntegrityViolationException integrityViolation(ConstraintKind kind, String constraintName, String sqlState) {
        SQLException sqlException = new SQLException("constraint violation", sqlState);
        return new DataIntegrityViolationException("could not execute statement", new ConstraintViolationException(
                "could not execute statement", sqlException, "insert into thanks_entry", kind, constraintName));
    }

    private void givenSaveAssignsId() {
        given(thanksEntryRepository.saveAndFlush(any(ThanksEntry.class))).willAnswer(invocation -> {
            ThanksEntry entry = invocation.getArgument(0);
            ReflectionTestUtils.setField(entry, "id", ENTRY_ID);
            return entry;
        });
    }

    // 서버 시계는 운영처럼 UTC로 두고, 지금을 한국 시각으로 적어 고정한다
    private ThanksEntryService serviceAt(String koreanDateTime) {
        Instant now = LocalDateTime.parse(koreanDateTime).atZone(KST).toInstant();
        return serviceWith(Clock.fixed(now, ZoneOffset.UTC));
    }

    private ThanksEntryService serviceWith(Clock clock) {
        return new ThanksEntryService(thanksEntryRepository, thanksReactionRepository, thanksReactionService,
                thanksCommentRepository, thanksCommentService,
                userRepository, userProfileRepository, uploadedFileRepository, fileStorageService, clock);
    }

    private ThanksEntry createEntry(Long id, User user, ThanksSlot slot, UploadedFile file) {
        ThanksEntry entry = new ThanksEntry(user, MISSION_DATE, slot, "감사합니다", file);
        ReflectionTestUtils.setField(entry, "id", id);
        return entry;
    }

    // 업로드 기록의 createdBy는 presigned URL을 요청한 사용자다
    private UploadedFile createFile(Long id, String contentType, Long uploadedBy) {
        UploadedFile file = new UploadedFile("thanks", "stored-" + id, "uploads/stored-" + id, contentType, 1024L);
        ReflectionTestUtils.setField(file, "id", id);
        ReflectionTestUtils.setField(file, "createdBy", uploadedBy);
        return file;
    }

    private UserPrincipal principalOf(User user) {
        return new UserPrincipal(user, null);
    }

    private User createUser(Long id, String name, Role role) {
        User user = new User("user" + id + "@test.com", name, AuthProvider.KAKAO, "provider_" + id, role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
