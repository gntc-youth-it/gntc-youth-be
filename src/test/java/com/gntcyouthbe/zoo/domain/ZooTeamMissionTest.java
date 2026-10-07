package com.gntcyouthbe.zoo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ZooTeamMissionTest {

    @Test
    @DisplayName("다시 제출하면 사진과 답 목록이 통째로 바뀌고 수정 시각이 갱신된다")
    void resubmit() {
        // given
        ZooTeamMission mission = new ZooTeamMission(createTeam(), ZooStop.AFRICA_1, createPhoto("first.webp"),
                List.of(new ZooMissionAnswer("q1", "7개"), new ZooMissionAnswer("q2", "타조")));
        LocalDateTime beforeResubmit = LocalDateTime.now();
        UploadedFile newPhoto = createPhoto("second.webp");

        // when
        mission.resubmit(newPhoto, List.of(new ZooMissionAnswer("q1", "6개")));

        // then
        assertThat(mission.getPhoto()).isEqualTo(newPhoto);
        assertThat(mission.getAnswers())
                .extracting(ZooMissionAnswer::getQuestionId, ZooMissionAnswer::getAnswer)
                .containsExactly(tuple("q1", "6개"));
        assertThat(mission.getUpdatedAt()).isAfterOrEqualTo(beforeResubmit);
    }

    private ZooTeam createTeam() {
        User leader = new User("leader@example.com", "조장", AuthProvider.KAKAO, "kakao_1");
        ReflectionTestUtils.setField(leader, "id", 1L);
        return new ZooTeam("사자팀", ZooCourse.A, leader);
    }

    private UploadedFile createPhoto(String filename) {
        return new UploadedFile(filename, "stored_" + filename, "uploads/stored_" + filename, "image/webp", 1024L);
    }
}
