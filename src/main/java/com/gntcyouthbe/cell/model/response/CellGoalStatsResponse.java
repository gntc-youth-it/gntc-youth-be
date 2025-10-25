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

    private final int totalVerses;

    private final int memberCount;

    private final long totalCopies;

    private final long goalCopies;

    public CellGoalStatsResponse(Cell cell, CellGoal goal, CellGoalStats stats, int memberCount, long totalCopies) {
        this.cellId = cell.getId();
        this.cellName = cell.getName();
        this.title = goal.getTitle();
        this.progress = stats.getProgress();
        this.totalVerses = goal.getTotalVerses();
        this.memberCount = memberCount;
        this.totalCopies = totalCopies;
        this.goalCopies = (long) goal.getTotalVerses() * memberCount;
    }
}
