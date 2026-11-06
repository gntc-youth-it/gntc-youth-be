package com.gntcyouthbe.thanks.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CELL_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_CELL_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksCell;
import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import com.gntcyouthbe.thanks.model.request.ThanksMyCellUpdateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCellListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCellResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCellStatsResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCellStatsResponse.DailyCount;
import com.gntcyouthbe.thanks.model.response.ThanksCellStatsResponse.MemberStats;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.AuthorInfo;
import com.gntcyouthbe.thanks.repository.ThanksCellMemberRepository;
import com.gntcyouthbe.thanks.repository.ThanksCellRepository;
import com.gntcyouthbe.thanks.repository.ThanksDailyCount;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class ThanksCellServiceTest {

    private static final Long USER_ID = 5L;
    private static final Long UNKNOWN_CELL_ID = 999L;

    @Mock
    private ThanksCellRepository thanksCellRepository;

    @Mock
    private ThanksCellMemberRepository thanksCellMemberRepository;

    @Mock
    private ThanksEntryRepository thanksEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private ThanksCellService thanksCellService;

    private final User user = createUser(USER_ID);
    private final ThanksCell hallelujah = createCell(1L, "할렐루야구역", 1);
    private final ThanksCell holySpirit = createCell(2L, "성령충만구역", 2);

    @BeforeEach
    void setUp() {
        thanksCellService = new ThanksCellService(thanksCellRepository, thanksCellMemberRepository, thanksEntryRepository,
                userRepository, userProfileRepository, new TransactionTemplate(transactionManager));
    }

    // --- 목록 ---

    @Test
    @DisplayName("아직 안 고른 사람은 내 구역이 null이고, 구역은 저장소가 준 순서 그대로 담는다")
    void getCells_notChosen() {
        // given
        given(thanksCellMemberRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(thanksCellRepository.findAllByOrderBySortOrderAscIdAsc()).willReturn(List.of(hallelujah, holySpirit));

        // when
        ThanksCellListResponse response = thanksCellService.getCells(principalOf(user));

        // then
        assertThat(response.myCellId()).isNull();
        assertThat(response.cells()).containsExactly(
                new ThanksCellResponse(1L, "할렐루야구역"),
                new ThanksCellResponse(2L, "성령충만구역"));
    }

    @Test
    @DisplayName("고른 사람은 내 구역이 그 구역이다")
    void getCells_chosen() {
        // given
        given(thanksCellMemberRepository.findByUserId(USER_ID))
                .willReturn(Optional.of(new ThanksCellMember(user, holySpirit)));
        given(thanksCellRepository.findAllByOrderBySortOrderAscIdAsc()).willReturn(List.of(hallelujah, holySpirit));

        // when
        ThanksCellListResponse response = thanksCellService.getCells(principalOf(user));

        // then
        assertThat(response.myCellId()).isEqualTo(2L);
    }

    // --- 구역 현황 ---

    @Test
    @DisplayName("구역원이 보면 구역을 고른 사람 전부를 프로필 사진, 날짜별 개수, 가장 최근에 올린 시각과 함께 돌려준다")
    void getCellStats_member() {
        // given
        User noPost = createUser(7L);
        LocalDateTime earlier = LocalDateTime.of(2026, 11, 9, 3, 0);
        LocalDateTime latest = LocalDateTime.of(2026, 11, 9, 4, 12);
        given(thanksCellRepository.existsById(1L)).willReturn(true);
        given(thanksCellMemberRepository.findByCellIdWithUser(1L)).willReturn(List.of(
                new ThanksCellMember(user, hallelujah), new ThanksCellMember(noPost, hallelujah)));
        given(userProfileRepository.findProfileImagePaths(List.of(USER_ID, 7L)))
                .willReturn(Map.of(USER_ID, "uploads/me.jpeg"));
        given(thanksEntryRepository.countDailyByUserIds(List.of(USER_ID, 7L))).willReturn(List.of(
                new ThanksDailyCount(USER_ID, LocalDate.of(2026, 11, 9), 1L, earlier),
                new ThanksDailyCount(USER_ID, LocalDate.of(2026, 11, 8), 2L, latest)));

        // when
        ThanksCellStatsResponse response = thanksCellService.getCellStats(principalOf(user), 1L);

        // then
        assertThat(response.cellId()).isEqualTo(1L);
        assertThat(response.members()).containsExactly(
                new MemberStats(new AuthorInfo(USER_ID, "유저5", "uploads/me.jpeg"), latest, List.of(
                        new DailyCount(LocalDate.of(2026, 11, 9), 1),
                        new DailyCount(LocalDate.of(2026, 11, 8), 2))),
                new MemberStats(new AuthorInfo(7L, "유저7", null), null, List.of()));
    }

    @Test
    @DisplayName("MASTER는 구역원이 아니어도 볼 수 있고, 명단에는 구역을 고른 사람만 나온다")
    void getCellStats_master() {
        // given
        given(thanksCellRepository.existsById(1L)).willReturn(true);
        given(thanksCellMemberRepository.findByCellIdWithUser(1L)).willReturn(List.of(new ThanksCellMember(user, hallelujah)));
        given(userProfileRepository.findProfileImagePaths(List.of(USER_ID))).willReturn(Map.of());
        given(thanksEntryRepository.countDailyByUserIds(List.of(USER_ID))).willReturn(List.of());

        // when
        ThanksCellStatsResponse response = thanksCellService.getCellStats(principalOf(createUser(3L, Role.MASTER)), 1L);

        // then
        assertThat(response.members()).extracting(member -> member.user().userId()).containsExactly(USER_ID);
    }

    @Test
    @DisplayName("아무도 안 고른 구역은 빈 명단이고 프로필과 감사를 조회하지 않는다")
    void getCellStats_noMember() {
        // given
        given(thanksCellRepository.existsById(1L)).willReturn(true);
        given(thanksCellMemberRepository.findByCellIdWithUser(1L)).willReturn(List.of());

        // when
        ThanksCellStatsResponse response = thanksCellService.getCellStats(principalOf(createUser(3L, Role.MASTER)), 1L);

        // then
        assertThat(response.cellId()).isEqualTo(1L);
        assertThat(response.members()).isEmpty();
        then(userProfileRepository).shouldHaveNoInteractions();
        then(thanksEntryRepository).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, mode = EnumSource.Mode.EXCLUDE, names = "MASTER")
    @DisplayName("구역원이 아니면 MASTER가 아닌 어떤 역할도 6008이고, 프로필과 감사를 조회하지 않는다")
    void getCellStats_notMember(Role role) {
        // given
        given(thanksCellRepository.existsById(1L)).willReturn(true);
        given(thanksCellMemberRepository.findByCellIdWithUser(1L)).willReturn(List.of(new ThanksCellMember(user, hallelujah)));

        // when, then
        assertThatThrownBy(() -> thanksCellService.getCellStats(principalOf(createUser(9L, role)), 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(THANKS_CELL_ACCESS_DENIED.getMessage());
        then(userProfileRepository).shouldHaveNoInteractions();
        then(thanksEntryRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("없는 구역이면 볼 권한을 따지기 전에 1201이다")
    void getCellStats_cellNotFound() {
        // given
        given(thanksCellRepository.existsById(UNKNOWN_CELL_ID)).willReturn(false);

        // when, then
        assertThatThrownBy(() -> thanksCellService.getCellStats(principalOf(user), UNKNOWN_CELL_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(CELL_NOT_FOUND.getMessage());
        then(thanksCellMemberRepository).shouldHaveNoInteractions();
    }

    // --- 내 구역 정하기 ---

    @Test
    @DisplayName("처음 고르면 사람과 구역을 잇는 행을 저장하고, 목록과 방금 정한 구역을 돌려준다")
    void updateMyCell_first() {
        // given
        given(thanksCellRepository.findById(2L)).willReturn(Optional.of(holySpirit));
        given(thanksCellMemberRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(thanksCellRepository.findAllByOrderBySortOrderAscIdAsc()).willReturn(List.of(hallelujah, holySpirit));

        // when
        ThanksCellListResponse response = thanksCellService.updateMyCell(principalOf(user), new ThanksMyCellUpdateRequest(2L));

        // then
        ArgumentCaptor<ThanksCellMember> saved = ArgumentCaptor.forClass(ThanksCellMember.class);
        then(thanksCellMemberRepository).should().save(saved.capture());
        assertThat(saved.getValue().getUser()).isEqualTo(user);
        assertThat(saved.getValue().getCell()).isEqualTo(holySpirit);
        assertThat(response.myCellId()).isEqualTo(2L);
        assertThat(response.cells()).extracting(ThanksCellResponse::id).containsExactly(1L, 2L);
        then(transactionManager).should().commit(any());
    }

    @Test
    @DisplayName("이미 고른 사람이 다시 보내면 행을 새로 만들지 않고 구역만 바꾼다")
    void updateMyCell_change() {
        // given
        ThanksCellMember member = new ThanksCellMember(user, hallelujah);
        given(thanksCellRepository.findById(2L)).willReturn(Optional.of(holySpirit));
        given(thanksCellMemberRepository.findByUserId(USER_ID)).willReturn(Optional.of(member));
        given(thanksCellRepository.findAllByOrderBySortOrderAscIdAsc()).willReturn(List.of(hallelujah, holySpirit));

        // when
        ThanksCellListResponse response = thanksCellService.updateMyCell(principalOf(user), new ThanksMyCellUpdateRequest(2L));

        // then
        assertThat(member.getCell()).isEqualTo(holySpirit);
        assertThat(response.myCellId()).isEqualTo(2L);
        then(thanksCellMemberRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("없는 구역이면 1201이고 아무것도 저장하지 않는다")
    void updateMyCell_cellNotFound() {
        // given
        given(thanksCellRepository.findById(UNKNOWN_CELL_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> thanksCellService.updateMyCell(principalOf(user), new ThanksMyCellUpdateRequest(UNKNOWN_CELL_ID)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(CELL_NOT_FOUND.getMessage());
        then(thanksCellMemberRepository).shouldHaveNoInteractions();
        then(transactionManager).should().rollback(any());
    }

    @Test
    @DisplayName("없는 사용자면 1101이다")
    void updateMyCell_userNotFound() {
        // given
        given(thanksCellRepository.findById(2L)).willReturn(Optional.of(holySpirit));
        given(thanksCellMemberRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> thanksCellService.updateMyCell(principalOf(user), new ThanksMyCellUpdateRequest(2L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(USER_NOT_FOUND.getMessage());
        then(thanksCellMemberRepository).should(never()).save(any());
    }

    // --- 처음 고르는 요청이 겹칠 때 ---

    @Test
    @DisplayName("INSERT가 unique 제약에 걸리면 그 트랜잭션을 되돌리고, 새 트랜잭션에서 먼저 들어간 행의 구역을 바꾼다")
    void updateMyCell_uniqueViolation() {
        // given
        ThanksCellMember insertedFirst = new ThanksCellMember(user, hallelujah);
        given(thanksCellRepository.findById(2L)).willReturn(Optional.of(holySpirit));
        given(thanksCellMemberRepository.findByUserId(USER_ID))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(insertedFirst));
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(thanksCellMemberRepository.save(any())).willThrow(new DataIntegrityViolationException("unique 위반"));
        given(thanksCellRepository.findAllByOrderBySortOrderAscIdAsc()).willReturn(List.of(hallelujah, holySpirit));

        // when
        ThanksCellListResponse response = thanksCellService.updateMyCell(principalOf(user), new ThanksMyCellUpdateRequest(2L));

        // then
        assertThat(insertedFirst.getCell()).isEqualTo(holySpirit);
        assertThat(response.myCellId()).isEqualTo(2L);
        then(transactionManager).should(times(2)).getTransaction(any());
        then(transactionManager).should().rollback(any());
        then(transactionManager).should().commit(any());
    }

    @Test
    @DisplayName("다시 해도 무결성 오류가 나면 그대로 던지고, 더 시도하지 않는다")
    void updateMyCell_integrityViolationAgain() {
        // given
        given(thanksCellRepository.findById(2L)).willReturn(Optional.of(holySpirit));
        given(thanksCellMemberRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(thanksCellMemberRepository.save(any())).willThrow(new DataIntegrityViolationException("not null 위반"));

        // when, then
        assertThatThrownBy(() -> thanksCellService.updateMyCell(principalOf(user), new ThanksMyCellUpdateRequest(2L)))
                .isInstanceOf(DataIntegrityViolationException.class);
        then(thanksCellMemberRepository).should(times(2)).save(any());
        then(transactionManager).should(times(2)).rollback(any());
        then(transactionManager).should(never()).commit(any());
    }

    private UserPrincipal principalOf(User user) {
        return new UserPrincipal(user, null);
    }

    private static User createUser(Long id) {
        return createUser(id, Role.USER);
    }

    private static User createUser(Long id, Role role) {
        User user = new User("user" + id + "@example.com", "유저" + id, AuthProvider.KAKAO, "kakao_" + id, role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static ThanksCell createCell(Long id, String name, int sortOrder) {
        ThanksCell cell = new ThanksCell(name, sortOrder);
        ReflectionTestUtils.setField(cell, "id", id);
        return cell;
    }
}
