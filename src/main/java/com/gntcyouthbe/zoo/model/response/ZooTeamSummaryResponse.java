package com.gntcyouthbe.zoo.model.response;

import com.gntcyouthbe.zoo.domain.ZooCourse;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamStatus;
import java.time.LocalDateTime;

public record ZooTeamSummaryResponse(
        Long id,
        String name,
        ZooCourse course,
        ZooTeamStatus status,
        String leaderName,
        long memberCount,
        long arrivedCount,
        LocalDateTime createdAt
) {

    public static ZooTeamSummaryResponse of(ZooTeam team, long memberCount, long arrivedCount) {
        return new ZooTeamSummaryResponse(
                team.getId(),
                team.getName(),
                team.getCourse(),
                team.getStatus(),
                team.getLeader().getName(),
                memberCount,
                arrivedCount,
                team.getCreatedAt()
        );
    }
}
