package com.gntcyouthbe.cell.repository;

import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CellMemberRepository extends JpaRepository<CellMember, Long> {
    Optional<CellMember> findByUserId(Long userId);
    List<CellMember> findAllByCell(Cell cell);
}
