package com.gntcyouthbe.zoo.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ZooTeamTest {

    @Test
    @DisplayName("조를 만들면 모집 중 상태이고 출발 시각이 없다")
    void create() {
        // when
        ZooTeam team = new ZooTeam("사자팀", ZooCourse.A, createUser(1L));

        // then
        assertThat(team.getStatus()).isEqualTo(ZooTeamStatus.RECRUITING);
        assertThat(team.isStarted()).isFalse();
        assertThat(team.getStartedAt()).isNull();
    }

    @Test
    @DisplayName("마감하면 출발 상태가 되고 출발 시각이 기록된다")
    void start() {
        // given
        ZooTeam team = new ZooTeam("사자팀", ZooCourse.A, createUser(1L));

        // when
        team.start();

        // then
        assertThat(team.getStatus()).isEqualTo(ZooTeamStatus.STARTED);
        assertThat(team.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 출발한 조를 다시 마감해도 처음 출발한 시각을 유지한다")
    void start_twice_keepsFirstStartedAt() {
        // given
        ZooTeam team = new ZooTeam("사자팀", ZooCourse.A, createUser(1L));
        team.start();
        LocalDateTime firstStartedAt = team.getStartedAt();

        // when
        team.start();

        // then
        assertThat(team.getStartedAt()).isEqualTo(firstStartedAt);
    }

    @Test
    @DisplayName("조장을 넘기면 새 조장만 조장으로 판단된다")
    void changeLeader() {
        // given
        ZooTeam team = new ZooTeam("사자팀", ZooCourse.A, createUser(1L));

        // when
        team.changeLeader(createUser(2L));

        // then
        assertThat(team.isLeader(2L)).isTrue();
        assertThat(team.isLeader(1L)).isFalse();
    }

    private User createUser(Long id) {
        User user = new User("user" + id + "@example.com", "사용자" + id, AuthProvider.KAKAO, "kakao_" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
