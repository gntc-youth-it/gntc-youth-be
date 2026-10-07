package com.gntcyouthbe.zoo.model.response;

import com.gntcyouthbe.zoo.domain.ZooMissionAnswer;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import java.time.LocalDateTime;
import java.util.List;

// 조장이 답만 고칠 때 사진을 다시 올리지 않고 photoFileId를 그대로 보내므로 경로와 함께 내려준다
public record ZooMissionResponse(
        ZooStop stopId,
        List<AnswerInfo> answers,
        Long photoFileId,
        String photoPath,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {

    public record AnswerInfo(
            String questionId,
            String answer
    ) {
        public static AnswerInfo from(ZooMissionAnswer answer) {
            return new AnswerInfo(answer.getQuestionId(), answer.getAnswer());
        }
    }

    public static ZooMissionResponse from(ZooTeamMission mission) {
        return new ZooMissionResponse(
                mission.getStop(),
                mission.getAnswers().stream().map(AnswerInfo::from).toList(),
                mission.getPhoto().getId(),
                mission.getPhoto().getFilePath(),
                mission.getCreatedAt(),
                mission.getUpdatedAt()
        );
    }
}
