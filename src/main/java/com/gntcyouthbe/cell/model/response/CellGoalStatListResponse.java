package com.gntcyouthbe.cell.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.cell.domain.Cell;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CellGoalStatListResponse {

    private final Long userCellId;

    private final List<CellGoalStatsResponse> cellGoalStats;

    public CellGoalStatListResponse(Cell userCell, List<CellGoalStatsResponse> stats) {
        this.userCellId = userCell.getId();
        this.cellGoalStats = stats;
    }
}
