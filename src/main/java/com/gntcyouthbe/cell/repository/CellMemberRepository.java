package com.gntcyouthbe.cell.repository;

import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellMember;
import com.gntcyouthbe.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CellMemberRepository extends JpaRepository<CellMember, Long> {
    Optional<CellMember> findByUserId(Long userId);
    Optional<CellMember> findByUser(User user);
    List<CellMember> findAllByCell(Cell cell);
}
