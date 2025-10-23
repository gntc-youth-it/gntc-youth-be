package com.gntcyouthbe.cell.domain;

public class CellGoalStats {
    private final double progress;

    public CellGoalStats(final CellGoal goal, final CellMembers members, final long totalCopiesCount) {
        if (totalCopiesCount == 0) {
            this.progress = 0.0;
            return;
        }
        long completedCopiesCount = members.getCompletedCopiesCountForGoal(goal);
        this.progress = (double) completedCopiesCount / totalCopiesCount * 100;
    }
}
