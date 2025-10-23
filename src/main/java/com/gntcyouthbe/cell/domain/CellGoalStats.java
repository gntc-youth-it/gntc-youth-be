package com.gntcyouthbe.cell.domain;

import lombok.Getter;

@Getter
public class CellGoalStats {
    private final double progress;

    public CellGoalStats(final CellGoal goal, final CellMembers members, final long totalCopiesCount) {
        if (totalCopiesCount == 0) {
            this.progress = 0.0;
            return;
        }
        final long goalCopiesCount =  (long) goal.getTotalVerses() * members.getMemberCount();
        this.progress = (double) totalCopiesCount / goalCopiesCount;
    }
}
