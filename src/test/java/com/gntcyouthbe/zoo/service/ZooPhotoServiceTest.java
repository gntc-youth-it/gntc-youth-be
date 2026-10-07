package com.gntcyouthbe.zoo.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_ADMIN_ONLY;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_OWN_TEAM_PHOTO;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_PHOTO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import com.gntcyouthbe.zoo.domain.ZooCourse;
import com.gntcyouthbe.zoo.domain.ZooMissionAnswer;
import com.gntcyouthbe.zoo.domain.ZooPhotoVote;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import com.gntcyouthbe.zoo.model.response.ZooPhotoListResponse;
import com.gntcyouthbe.zoo.model.response.ZooPhotoResponse;
import com.gntcyouthbe.zoo.model.response.ZooPhotoResultResponse;
import com.gntcyouthbe.zoo.repository.ZooPhotoVoteRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMemberRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ZooPhotoServiceTest {

    @Mock
    private ZooTeamMissionRepository zooTeamMissionRepository;

    @Mock
    private ZooTeamMemberRepository zooTeamMemberRepository;

    @Mock
    private ZooPhotoVoteRepository zooPhotoVoteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ZooPhotoService zooPhotoService;

    private final User voter = createUser(5L, Role.USER);
    private final User master = createUser(3L, Role.MASTER);
    private final ZooTeam lionTeam = createTeam(10L, "사자팀");
    private final ZooTeam tigerTeam = createTeam(20L, "호랑이팀");

    @Test
    @DisplayName("사진 목록에 내 조 사진인지와 내가 투표했는지를 표시한다")
    void getPhotos_marksMyTeamAndVoted() {
        // given
        given(zooTeamMemberRepository.findTeamIdByUserId(5L)).willReturn(Optional.of(10L));
        given(zooPhotoVoteRepository.findMissionIdsByUserId(5L)).willReturn(List.of(2L));
        given(zooTeamMissionRepository.findAllWithTeamAndPhoto()).willReturn(List.of(
                createMission(1L, lionTeam, ZooStop.AFRICA_1),
                createMission(2L, tigerTeam, ZooStop.AFRICA_1)));

        // when
        ZooPhotoListResponse response = zooPhotoService.getPhotos(principalOf(voter));

        // then
        assertThat(response.photos())
                .extracting(ZooPhotoResponse::id, ZooPhotoResponse::isMyTeam, ZooPhotoResponse::isVoted)
                .containsExactly(tuple(1L, true, false), tuple(2L, false, true));
    }

    @Test
    @DisplayName("다른 조 사진에 투표하면 표가 저장된다")
    void vote_success() {
        // given
        given(zooTeamMissionRepository.findByIdForUpdate(1L)).willReturn(Optional.of(createMission(1L, lionTeam, ZooStop.AFRICA_1)));
        given(zooTeamMemberRepository.existsByTeamIdAndUserId(10L, 5L)).willReturn(false);
        given(zooPhotoVoteRepository.existsByMissionIdAndUserId(1L, 5L)).willReturn(false);
        given(userRepository.findById(5L)).willReturn(Optional.of(voter));

        // when
        zooPhotoService.vote(principalOf(voter), 1L);

        // then
        then(zooPhotoVoteRepository).should().save(any(ZooPhotoVote.class));
    }

    @Test
    @DisplayName("이미 투표한 사진에 다시 투표하면 저장하지 않는다")
    void vote_alreadyVoted() {
        // given
        given(zooTeamMissionRepository.findByIdForUpdate(1L)).willReturn(Optional.of(createMission(1L, lionTeam, ZooStop.AFRICA_1)));
        given(zooTeamMemberRepository.existsByTeamIdAndUserId(10L, 5L)).willReturn(false);
        given(zooPhotoVoteRepository.existsByMissionIdAndUserId(1L, 5L)).willReturn(true);

        // when
        zooPhotoService.vote(principalOf(voter), 1L);

        // then
        then(zooPhotoVoteRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("우리 조 사진에는 투표할 수 없다")
    void vote_ownTeamPhoto() {
        // given
        given(zooTeamMissionRepository.findByIdForUpdate(1L)).willReturn(Optional.of(createMission(1L, lionTeam, ZooStop.AFRICA_1)));
        given(zooTeamMemberRepository.existsByTeamIdAndUserId(10L, 5L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> zooPhotoService.vote(principalOf(voter), 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_OWN_TEAM_PHOTO.getCode());

        then(zooPhotoVoteRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("없는 사진에는 투표할 수 없다")
    void vote_photoNotFound() {
        // given
        given(zooTeamMissionRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> zooPhotoService.vote(principalOf(voter), 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_PHOTO_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("없는 사진의 투표는 취소할 수 없다")
    void cancelVote_photoNotFound() {
        // given
        given(zooTeamMissionRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> zooPhotoService.cancelVote(principalOf(voter), 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_PHOTO_NOT_FOUND.getCode());

        then(zooPhotoVoteRepository).should(never()).deleteByMissionIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("결과는 득표 수가 많은 순, 같으면 id 순이고 표를 못 받은 사진도 0표로 들어간다")
    void getResults_sortedWithZeroVotes() {
        // given
        given(zooPhotoVoteRepository.countGroupByMissionId()).willReturn(List.of(
                new Object[]{3L, 2L},
                new Object[]{2L, 2L}));
        given(zooTeamMissionRepository.findAllWithTeamAndPhoto()).willReturn(List.of(
                createMission(1L, lionTeam, ZooStop.AFRICA_1),
                createMission(2L, lionTeam, ZooStop.AUSTRALIA),
                createMission(3L, tigerTeam, ZooStop.AFRICA_1)));
        given(zooPhotoVoteRepository.countDistinctVoters()).willReturn(3L);

        // when
        ZooPhotoResultResponse response = zooPhotoService.getResults(principalOf(master));

        // then
        assertThat(response.voterCount()).isEqualTo(3L);
        assertThat(response.photos())
                .extracting(ZooPhotoResultResponse.PhotoResult::id, ZooPhotoResultResponse.PhotoResult::voteCount)
                .containsExactly(tuple(2L, 2L), tuple(3L, 2L), tuple(1L, 0L));
    }

    @Test
    @DisplayName("MASTER가 아니면 투표 결과를 볼 수 없다")
    void getResults_notMaster() {
        // when & then
        assertThatThrownBy(() -> zooPhotoService.getResults(principalOf(voter)))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_ADMIN_ONLY.getCode());
    }

    private ZooTeamMission createMission(Long id, ZooTeam team, ZooStop stop) {
        UploadedFile photo = new UploadedFile("zoo-mission.webp", "stored.webp", "uploads/stored-" + id + ".webp", "image/webp", 1024L);
        ZooTeamMission mission = new ZooTeamMission(team, stop, photo, List.of(new ZooMissionAnswer("q1", "7개")));
        ReflectionTestUtils.setField(mission, "id", id);
        return mission;
    }

    private ZooTeam createTeam(Long id, String name) {
        ZooTeam team = new ZooTeam(name, ZooCourse.A, createUser(id + 100, Role.USER));
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private UserPrincipal principalOf(User user) {
        return new UserPrincipal(user, null);
    }

    private User createUser(Long id, Role role) {
        User user = new User("user" + id + "@test.com", "사용자" + id, AuthProvider.KAKAO, "provider_" + id);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }
}
