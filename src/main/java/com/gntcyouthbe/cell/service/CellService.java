package com.gntcyouthbe.cell.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CELL_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.model.response.CellListResponse;
import com.gntcyouthbe.cell.repository.CellMemberRepository;
import com.gntcyouthbe.cell.repository.CellRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CellService {

    private final CellRepository cellRepository;
    private final UserRepository userRepository;
    private final CellMemberRepository memberRepository;

    @Transactional(readOnly = true)
    public CellListResponse getCellList() {
        final List<Cell> cells = cellRepository.findAll();
        return new CellListResponse(cells);
    }

    @Transactional
    public void joinCell(final Long cellId, final UserPrincipal userPrincipal) {
        final User user = getUser(userPrincipal);
        withdrawFromCell(user);
        final Cell cell = getCell(cellId);
        memberRepository.save(cell.join(user));
    }

    private User getUser(final UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    private void withdrawFromCell(final User user) {
        memberRepository.findByUser(user)
                .ifPresent(memberRepository::delete);
    }

    private Cell getCell(final Long cellId) {
        return cellRepository.findById(cellId)
                .orElseThrow(() -> new EntityNotFoundException(CELL_NOT_FOUND));
    }
}
