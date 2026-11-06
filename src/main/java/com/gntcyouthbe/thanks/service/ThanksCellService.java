package com.gntcyouthbe.thanks.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksCell;
import com.gntcyouthbe.thanks.domain.ThanksCellMember;
import com.gntcyouthbe.thanks.model.request.ThanksMyCellUpdateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCellListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCellResponse;
import com.gntcyouthbe.thanks.repository.ThanksCellMemberRepository;
import com.gntcyouthbe.thanks.repository.ThanksCellRepository;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CELL_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ThanksCellService {

    private final ThanksCellRepository thanksCellRepository;
    private final ThanksCellMemberRepository thanksCellMemberRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public ThanksCellListResponse getCells(UserPrincipal userPrincipal) {
        Long myCellId = thanksCellMemberRepository.findByUserId(userPrincipal.getUserId())
                .map(member -> member.getCell().getId())
                .orElse(null);
        return responseOf(myCellId);
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
