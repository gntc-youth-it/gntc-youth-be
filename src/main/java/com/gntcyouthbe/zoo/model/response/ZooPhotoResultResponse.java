package com.gntcyouthbe.zoo.model.response;

import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import java.util.List;

// 운영자용 투표 결과. 표를 못 받은 사진도 voteCount 0으로 모두 넣는다
public record ZooPhotoResultResponse(
        long voterCount,
        List<PhotoResult> photos
) {

    public record PhotoResult(
            Long id,
            ZooStop stopId,
            Long teamId,
            String teamName,
            String photoPath,
            long voteCount
    ) {
        public static PhotoResult of(ZooTeamMission mission, long voteCount) {
            return new PhotoResult(
                    mission.getId(),
                    mission.getStop(),
                    mission.getTeam().getId(),
                    mission.getTeam().getName(),
                    mission.getPhoto().getFilePath(),
                    voteCount
            );
        }
    }
}
