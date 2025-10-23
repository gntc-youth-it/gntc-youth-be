package com.gntcyouthbe.cell.repository;

import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CellGoalRepository extends JpaRepository<CellGoal, Long> {
    Optional<CellGoal> findByCell(Cell cell);
}
