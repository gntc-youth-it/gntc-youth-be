package com.gntcyouthbe.cell.service;

import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellGoal;
import com.gntcyouthbe.cell.domain.CellGoalStats;
import com.gntcyouthbe.cell.domain.CellMember;
import com.gntcyouthbe.cell.domain.CellMembers;
import com.gntcyouthbe.cell.model.response.CellGoalStatsResponse;
import com.gntcyouthbe.cell.repository.CellGoalRepository;
import com.gntcyouthbe.cell.repository.CellMemberRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.model.ExceptionCode;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CELL_MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CellGoalService {

    private final CellGoalRepository goalRepository;
    private final CellMemberRepository memberRepository;
    private final VerseCopyRepository copyRepository;

    @Transactional(readOnly = true)
    public CellGoalStatsResponse getGoalStats(final UserPrincipal userPrincipal) {
        final Cell cell = getCellByUserPrincipal(userPrincipal);
        final CellGoal goal = getCellGoal(cell);
        final CellMembers members = getCellMembers(cell);
        final long totalCopiesCount = countTotalCopies(goal, members);
        final CellGoalStats stats = new CellGoalStats(goal, members , totalCopiesCount);
        return new CellGoalStatsResponse(cell, goal, stats, members.getMemberCount(), totalCopiesCount);
    }

    private Cell getCellByUserPrincipal(final UserPrincipal userPrincipal) {
        final CellMember member = getCellMember(userPrincipal);
        return member.getCell();
    }

    private CellMember getCellMember(final UserPrincipal userPrincipal) {
        return memberRepository.findByUserId(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.CELL_MEMBER_NOT_FOUND));
    }

    private CellGoal getCellGoal(final Cell cell) {
        return goalRepository.findByCell(cell)
                .orElseThrow(() -> new EntityNotFoundException(CELL_MEMBER_NOT_FOUND));
    }

    private CellMembers getCellMembers(final Cell cell) {
        return new CellMembers(memberRepository.findAllByCell(cell));
    }

    private long countTotalCopies(final CellGoal goal, final CellMembers members) {
        return copyRepository.countByUserInAndVerse_SequenceBetween(members.getUsers(), goal.getStartSequence(), goal.getEndSequence());
    }
}
