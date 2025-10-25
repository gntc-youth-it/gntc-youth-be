package com.gntcyouthbe.cell.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.cell.domain.Cell;
import com.gntcyouthbe.cell.domain.CellGoal;
import com.gntcyouthbe.cell.domain.CellGoalStats;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CellGoalStatsResponse {

    private final Long cellId;

    private final String cellName;

    private final String title;

    private final double progress;

    public CellGoalStatsResponse(Cell cell, CellGoal goal, CellGoalStats stats) {
        this.cellId = cell.getId();
        this.cellName = cell.getName();
        this.title = goal.getTitle();
        this.progress = stats.getProgress();
    }
}

