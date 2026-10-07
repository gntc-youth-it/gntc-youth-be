package com.gntcyouthbe.zoo.model.response;

import com.gntcyouthbe.zoo.domain.ZooCourse;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import com.gntcyouthbe.zoo.domain.ZooTeamStatus;
import java.util.List;

// 행사 후 운영진이 채점할 때 보는 전체 조의 제출 내용
public record ZooMissionResultResponse(
        List<TeamResult> teams
) {

    public record TeamResult(
            Long id,
            String name,
            ZooCourse course,
            ZooTeamStatus status,
            String leaderName,
            List<String> members,
            List<ZooMissionResponse> missions
    ) {
        public static TeamResult of(ZooTeam team, List<String> memberNames, List<ZooTeamMission> missions) {
            return new TeamResult(
                    team.getId(),
                    team.getName(),
                    team.getCourse(),
                    team.getStatus(),
                    team.getLeader().getName(),
                    memberNames,
                    missions.stream().map(ZooMissionResponse::from).toList()
            );
        }
    }
}
