package com.gntcyouthbe.cell.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.cell.domain.Cell;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CellListResponse {
    private final List<CellResponse> cellInfos;

    public CellListResponse(List<Cell> cells) {
        this.cellInfos = cells.stream()
                .map(CellResponse::new)
                .toList();
    }
}
