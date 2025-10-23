package com.gntcyouthbe.cell.repository;

import com.gntcyouthbe.cell.domain.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CellRepository extends JpaRepository<Cell, Long> {

}
