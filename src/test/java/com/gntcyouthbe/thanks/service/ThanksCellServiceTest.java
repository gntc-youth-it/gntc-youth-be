package com.gntcyouthbe.thanks.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CELL_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksCell;
import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import com.gntcyouthbe.thanks.model.request.ThanksMyCellUpdateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCellListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCellResponse;
import com.gntcyouthbe.thanks.repository.ThanksCellMemberRepository;
import com.gntcyouthbe.thanks.repository.ThanksCellRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private UserRepository userRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private ThanksCellService thanksCellService;

    private final User user = createUser(USER_ID);
    private final ThanksCell hallelujah = createCell(1L, "할렐루야구역", 1);
    private final ThanksCell holySpirit = createCell(2L, "성령충만구역", 2);

    @BeforeEach
    void setUp() {
        thanksCellService = new ThanksCellService(thanksCellRepository, thanksCellMemberRepository, userRepository,
                new TransactionTemplate(transactionManager));
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
        User user = new User("user" + id + "@example.com", "유저" + id, AuthProvider.KAKAO, "kakao_" + id, Role.USER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static ThanksCell createCell(Long id, String name, int sortOrder) {
        ThanksCell cell = new ThanksCell(name, sortOrder);
        ReflectionTestUtils.setField(cell, "id", id);
        return cell;
    }
}
