package com.gntcyouthbe.cell.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.cell.domain.Cell;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CellResponse {

    private final Long cellId;

    private final String cellName;

    public CellResponse(Cell cell) {
        this.cellId = cell.getId();
        this.cellName = cell.getName();
    }
}
