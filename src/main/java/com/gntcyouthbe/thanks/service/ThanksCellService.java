package com.gntcyouthbe.thanks.service;

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
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CELL_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_CELL_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ThanksCellService {

    private final ThanksCellRepository thanksCellRepository;
    private final ThanksCellMemberRepository thanksCellMemberRepository;
    private final ThanksEntryRepository thanksEntryRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public ThanksCellListResponse getCells(UserPrincipal userPrincipal) {
        Long myCellId = thanksCellMemberRepository.findByUserId(userPrincipal.getUserId())
                .map(member -> member.getCell().getId())
                .orElse(null);
        return responseOf(myCellId);
    }

    // FE가 보는 동안 30초마다 부르는 API라서 사람마다 따로 조회하지 않는다.
    // 사람의 지금 구역으로 묶으므로, 구역을 바꾸면 지난 기록도 새 구역에서 보인다.
    @Transactional(readOnly = true)
    public ThanksCellStatsResponse getCellStats(UserPrincipal userPrincipal, Long cellId) {
        if (!thanksCellRepository.existsById(cellId)) {
            throw new EntityNotFoundException(CELL_NOT_FOUND);
        }
        List<ThanksCellMember> members = thanksCellMemberRepository.findByCellIdWithUser(cellId);
        // 권한 오류는 401로 바뀌지 않도록 @PreAuthorize가 아니라 여기서 403으로 던진다
        if (userPrincipal.getRole() != Role.MASTER && !isMember(members, userPrincipal.getUserId())) {
            throw new ForbiddenException(THANKS_CELL_ACCESS_DENIED);
        }
        if (members.isEmpty()) {
            return new ThanksCellStatsResponse(cellId, List.of());
        }

        List<Long> userIds = members.stream().map(member -> member.getUser().getId()).toList();
        Map<Long, String> profileImagePaths = userProfileRepository.findProfileImagePaths(userIds);
        Map<Long, List<ThanksDailyCount>> dailyCounts = thanksEntryRepository.countDailyByUserIds(userIds).stream()
                .collect(Collectors.groupingBy(ThanksDailyCount::userId));
        List<MemberStats> memberStats = members.stream()
                .map(ThanksCellMember::getUser)
                .map(user -> statsOf(user, profileImagePaths.get(user.getId()), dailyCounts.getOrDefault(user.getId(), List.of())))
                .toList();
        return new ThanksCellStatsResponse(cellId, memberStats);
    }

    // 잠그지 않는다. 처음 고르는 요청이 겹치면 늦은 쪽의 INSERT가 unique 제약에 걸리는데, PostgreSQL에서는 실패한
    // INSERT 뒤로 그 트랜잭션을 쓸 수 없으므로 트랜잭션을 되돌리고 새 트랜잭션에서 한 번 더 한다. 그때는 먼저 들어간 행이
    // 보여서 구역만 바꾸므로, 나중에 도착한 쪽이 이긴다. 다른 무결성 오류라면 다시 해도 똑같이 실패해서 그대로 던져진다.
    public ThanksCellListResponse updateMyCell(UserPrincipal userPrincipal, ThanksMyCellUpdateRequest request) {
        Long userId = userPrincipal.getUserId();
        Long cellId = request.cellId();
        try {
            transactionTemplate.executeWithoutResult(status -> saveMyCell(userId, cellId));
        } catch (DataIntegrityViolationException e) {
            transactionTemplate.executeWithoutResult(status -> saveMyCell(userId, cellId));
        }
        return responseOf(cellId);
    }

    private boolean isMember(List<ThanksCellMember> members, Long userId) {
        return members.stream().anyMatch(member -> member.getUser().getId().equals(userId));
    }

    private MemberStats statsOf(User user, String profileImagePath, List<ThanksDailyCount> counts) {
        LocalDateTime lastPostedAt = counts.stream()
                .map(ThanksDailyCount::lastPostedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);
        List<DailyCount> dailyCounts = counts.stream()
                .map(count -> new DailyCount(count.missionDate(), count.count()))
                .toList();
        return new MemberStats(AuthorInfo.of(user, profileImagePath), lastPostedAt, dailyCounts);
    }

    private void saveMyCell(Long userId, Long cellId) {
        ThanksCell cell = thanksCellRepository.findById(cellId)
                .orElseThrow(() -> new EntityNotFoundException(CELL_NOT_FOUND));
        thanksCellMemberRepository.findByUserId(userId).ifPresentOrElse(
                member -> member.changeCell(cell),
                () -> thanksCellMemberRepository.save(new ThanksCellMember(findUser(userId), cell)));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    private ThanksCellListResponse responseOf(Long myCellId) {
        List<ThanksCellResponse> cells = thanksCellRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(ThanksCellResponse::from)
                .toList();
        return new ThanksCellListResponse(cells, myCellId);
    }
}
