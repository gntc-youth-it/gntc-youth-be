package com.gntcyouthbe.cell.service;

import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.model.response.CellListResponse;
import com.gntcyouthbe.cell.repository.CellRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CellService {

    private final CellRepository cellRepository;

    @Transactional(readOnly = true)
    public CellListResponse getCellList() {
        List<Cell> cells = cellRepository.findAll();
        return new CellListResponse(cells);
    }
}
