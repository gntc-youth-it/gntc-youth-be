package com.gntcyouthbe.zoo.model.response;

import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.zoo.domain.ZooCourse;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamArrival;
import com.gntcyouthbe.zoo.domain.ZooTeamMember;
import com.gntcyouthbe.zoo.domain.ZooTeamStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ZooTeamDetailResponse(
        Long id,
        String name,
        ZooCourse course,
        ZooTeamStatus status,
        Long leaderUserId,
        List<MemberInfo> members,
        List<ArrivalInfo> arrivals,
        LocalDateTime createdAt,
        LocalDateTime startedAt
) {

    // record여야 isLeader 키가 그대로 나간다. Lombok @Getter + boolean isLeader는 "leader"로 직렬화된다.
    public record MemberInfo(
            Long userId,
            String name,
            String profileImagePath,
            boolean isLeader,
            LocalDateTime joinedAt
    ) {
        public static MemberInfo of(ZooTeamMember member, Long leaderUserId, String profileImagePath) {
            User user = member.getUser();
            return new MemberInfo(
                    user.getId(),
                    user.getName(),
                    profileImagePath,
                    user.getId().equals(leaderUserId),
                    member.getCreatedAt()
            );
        }
    }

    public record ArrivalInfo(
            ZooStop stopId,
            LocalDateTime arrivedAt
    ) {
        public static ArrivalInfo from(ZooTeamArrival arrival) {
            return new ArrivalInfo(arrival.getStop(), arrival.getCreatedAt());
        }
    }

    public static ZooTeamDetailResponse of(ZooTeam team, List<ZooTeamMember> members,
            Map<Long, String> profileImagePaths, List<ZooTeamArrival> arrivals) {
        Long leaderUserId = team.getLeader().getId();
        return new ZooTeamDetailResponse(
                team.getId(),
                team.getName(),
                team.getCourse(),
                team.getStatus(),
                leaderUserId,
                members.stream()
                        .map(member -> MemberInfo.of(member, leaderUserId,
                                profileImagePaths.get(member.getUser().getId())))
                        .toList(),
                arrivals.stream().map(ArrivalInfo::from).toList(),
                team.getCreatedAt(),
                team.getStartedAt()
        );
    }
}
