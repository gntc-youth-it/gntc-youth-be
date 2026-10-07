package com.gntcyouthbe.zoo.model.response;

import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import java.time.LocalDateTime;

// 투표 화면용 사진. 득표 수와 미션 답은 넣지 않는다.
// record여야 isMyTeam, isVoted 키가 그대로 나간다. Lombok @Getter + boolean isXxx는 xxx로 직렬화된다.
public record ZooPhotoResponse(
        Long id,
        ZooStop stopId,
        Long teamId,
        String teamName,
        String photoPath,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt,
        boolean isMyTeam,
        boolean isVoted
) {

    public static ZooPhotoResponse of(ZooTeamMission mission, boolean isMyTeam, boolean isVoted) {
        return new ZooPhotoResponse(
                mission.getId(),
                mission.getStop(),
                mission.getTeam().getId(),
                mission.getTeam().getName(),
                mission.getPhoto().getFilePath(),
                mission.getCreatedAt(),
                mission.getUpdatedAt(),
                isMyTeam,
                isVoted
        );
    }
}
