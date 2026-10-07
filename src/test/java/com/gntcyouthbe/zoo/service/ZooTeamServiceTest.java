package com.gntcyouthbe.zoo.service;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_ADMIN_ONLY;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_ALREADY_IN_TEAM;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_LEADER_CANNOT_LEAVE;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_NOT_TEAM_LEADER;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_NOT_TEAM_MEMBER;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_TEAM_ALREADY_STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import com.gntcyouthbe.zoo.domain.ZooCourse;
import com.gntcyouthbe.zoo.domain.ZooMissionAnswer;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamMember;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import com.gntcyouthbe.zoo.model.request.ZooMissionSubmitRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamCreateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamLeaderTransferRequest;
import com.gntcyouthbe.zoo.model.response.ZooTeamDetailResponse;
import com.gntcyouthbe.zoo.repository.ZooPhotoVoteRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamArrivalRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMemberRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ZooTeamServiceTest {

    private static final Long TEAM_ID = 10L;

    @Mock
    private ZooTeamRepository zooTeamRepository;

    @Mock
    private ZooTeamMemberRepository zooTeamMemberRepository;

    @Mock
    private ZooTeamArrivalRepository zooTeamArrivalRepository;

    @Mock
    private ZooTeamMissionRepository zooTeamMissionRepository;

    @Mock
    private ZooPhotoVoteRepository zooPhotoVoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private ZooTeamService zooTeamService;

    private final User leader = createUser(1L, "조장", Role.USER);
    private final User member = createUser(2L, "조원", Role.USER);
    private final User master = createUser(3L, "운영자", Role.MASTER);

    @Test
    @DisplayName("이미 조에 속한 사용자는 조를 만들 수 없다")
    void createTeam_alreadyInTeam() {
        // given
        given(zooTeamMemberRepository.existsByUserId(1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> zooTeamService.createTeam(principalOf(leader), new ZooTeamCreateRequest("사자팀", ZooCourse.A)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_ALREADY_IN_TEAM.getCode());

        then(zooTeamRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("조 만들기가 동시에 들어와 조원 유니크 제약에 걸리면 5002로 바꾼다")
    void createTeam_concurrentRequest_uniqueViolation() {
        // given
        given(zooTeamMemberRepository.existsByUserId(1L)).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(leader));
        given(zooTeamRepository.save(any(ZooTeam.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(zooTeamMemberRepository.saveAndFlush(any(ZooTeamMember.class)))
                .willThrow(new DataIntegrityViolationException("uk_zoo_team_member_user_id"));

        // when & then
        assertThatThrownBy(() -> zooTeamService.createTeam(principalOf(leader), new ZooTeamCreateRequest("사자팀", ZooCourse.A)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_ALREADY_IN_TEAM.getCode());
    }

    @Test
    @DisplayName("이미 이 조의 조원이 다시 참여하면 저장하지 않고 조 상세를 돌려준다")
    void joinTeam_alreadyMember_returnsDetail() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.existsByTeamIdAndUserId(TEAM_ID, 2L)).willReturn(true);

        // when
        ZooTeamDetailResponse response = zooTeamService.joinTeam(principalOf(member), TEAM_ID);

        // then
        assertThat(response.id()).isEqualTo(TEAM_ID);
        then(zooTeamMemberRepository).should(never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("출발한 조에는 참여할 수 없다")
    void joinTeam_started() {
        // given
        ZooTeam team = createTeam(leader);
        team.start();
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.existsByTeamIdAndUserId(TEAM_ID, 2L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> zooTeamService.joinTeam(principalOf(member), TEAM_ID))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_TEAM_ALREADY_STARTED.getCode());
    }

    @Test
    @DisplayName("두 조에 동시에 참여해 조원 유니크 제약에 걸리면 5002로 바꾼다")
    void joinTeam_concurrentRequest_uniqueViolation() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.existsByTeamIdAndUserId(TEAM_ID, 2L)).willReturn(false);
        given(zooTeamMemberRepository.existsByUserId(2L)).willReturn(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(member));
        given(zooTeamMemberRepository.saveAndFlush(any(ZooTeamMember.class)))
                .willThrow(new DataIntegrityViolationException("uk_zoo_team_member_user_id"));

        // when & then
        assertThatThrownBy(() -> zooTeamService.joinTeam(principalOf(member), TEAM_ID))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_ALREADY_IN_TEAM.getCode());
    }

    @Test
    @DisplayName("조장은 조에서 나갈 수 없다")
    void leaveTeam_leader() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.findByTeamIdAndUserId(TEAM_ID, 1L))
                .willReturn(Optional.of(new ZooTeamMember(team, leader)));

        // when & then
        assertThatThrownBy(() -> zooTeamService.leaveTeam(principalOf(leader), TEAM_ID))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_LEADER_CANNOT_LEAVE.getCode());

        then(zooTeamMemberRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("조장이 아닌 일반 사용자는 조를 삭제할 수 없다")
    void deleteTeam_notLeader() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));

        // when & then
        assertThatThrownBy(() -> zooTeamService.deleteTeam(principalOf(member), TEAM_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_NOT_TEAM_LEADER.getCode());

        then(zooTeamRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("조장은 출발한 조를 삭제할 수 없다")
    void deleteTeam_startedByLeader() {
        // given
        ZooTeam team = createTeam(leader);
        team.start();
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));

        // when & then
        assertThatThrownBy(() -> zooTeamService.deleteTeam(principalOf(leader), TEAM_ID))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_TEAM_ALREADY_STARTED.getCode());

        then(zooTeamRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("MASTER는 출발한 조도 사진 투표, 미션, 도착 기록, 조원과 함께 삭제할 수 있다")
    void deleteTeam_startedByMaster() {
        // given
        ZooTeam team = createTeam(leader);
        team.start();
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));

        // when
        zooTeamService.deleteTeam(principalOf(master), TEAM_ID);

        // then
        then(zooPhotoVoteRepository).should().deleteByTeamId(TEAM_ID);
        then(zooTeamMissionRepository).should().deleteAll(any());
        then(zooTeamArrivalRepository).should().deleteByTeamId(TEAM_ID);
        then(zooTeamMemberRepository).should().deleteByTeamId(TEAM_ID);
        then(zooTeamRepository).should().delete(team);
    }

    @Test
    @DisplayName("이미 기록한 장소에 다시 도착을 기록하면 저장하지 않는다")
    void markArrival_alreadyRecorded() {
        // given
        ZooTeam team = createTeam(leader);
        team.start();
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamArrivalRepository.existsByTeamIdAndStop(TEAM_ID, ZooStop.AFRICA_1)).willReturn(true);

        // when
        zooTeamService.markArrival(principalOf(leader), TEAM_ID, ZooStop.AFRICA_1);

        // then
        then(zooTeamArrivalRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("조원이 아닌 사람에게는 조장을 넘길 수 없다")
    void transferLeader_notMember() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.findByTeamIdAndUserId(TEAM_ID, 99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> zooTeamService.transferLeader(principalOf(leader), TEAM_ID,
                new ZooTeamLeaderTransferRequest(99L)))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_NOT_TEAM_MEMBER.getCode());

        assertThat(team.isLeader(1L)).isTrue();
    }

    @Test
    @DisplayName("다른 사람이 올린 파일은 미션 사진으로 쓸 수 없다")
    void submitMission_photoUploadedByOther() {
        // given
        ZooTeam team = createTeam(leader);
        team.start();
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMissionRepository.findByTeamIdAndStop(TEAM_ID, ZooStop.AFRICA_1)).willReturn(Optional.empty());
        given(uploadedFileRepository.findById(50L)).willReturn(Optional.of(createPhoto(50L, 99L)));

        // when & then
        assertThatThrownBy(() -> zooTeamService.submitMission(principalOf(leader), TEAM_ID, ZooStop.AFRICA_1,
                missionRequest(50L, "7개")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("code", FILE_NOT_FOUND.getCode());

        then(zooTeamMissionRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("다시 제출하면 이미 붙은 사진은 남이 올렸어도 쓸 수 있고, 답을 바꾸되 도착 기록은 새로 만들지 않는다")
    void submitMission_resubmitWithCurrentPhoto() {
        // given
        ZooTeam team = createTeam(leader);
        team.start();
        UploadedFile previousLeaderPhoto = createPhoto(50L, 2L);
        ZooTeamMission mission = new ZooTeamMission(team, ZooStop.AFRICA_1, previousLeaderPhoto,
                List.of(new ZooMissionAnswer("q1", "7개")));
        given(zooTeamRepository.findByIdForUpdate(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMissionRepository.findByTeamIdAndStop(TEAM_ID, ZooStop.AFRICA_1)).willReturn(Optional.of(mission));
        given(uploadedFileRepository.findById(50L)).willReturn(Optional.of(previousLeaderPhoto));
        given(zooTeamArrivalRepository.existsByTeamIdAndStop(TEAM_ID, ZooStop.AFRICA_1)).willReturn(true);

        // when
        zooTeamService.submitMission(principalOf(leader), TEAM_ID, ZooStop.AFRICA_1, missionRequest(50L, "6개"));

        // then
        assertThat(mission.getAnswers()).extracting(ZooMissionAnswer::getAnswer).containsExactly("6개");
        assertThat(mission.getUpdatedAt()).isNotNull();
        then(zooTeamMissionRepository).should(never()).save(any());
        then(zooTeamArrivalRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("조원이 아닌 사용자에게는 미션을 조회하지 않고 빈 배열로 보낸다")
    void getTeam_nonMember_hidesMissions() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.findByTeamIdWithUser(TEAM_ID)).willReturn(List.of(new ZooTeamMember(team, leader)));

        // when
        ZooTeamDetailResponse response = zooTeamService.getTeam(principalOf(member), TEAM_ID);

        // then
        assertThat(response.missions()).isEmpty();
        then(zooTeamMissionRepository).should(never()).findByTeamIdWithPhotoAndAnswers(any());
    }

    @Test
    @DisplayName("MASTER는 조원이 아니어도 미션을 볼 수 있다")
    void getTeam_master_seesMissions() {
        // given
        ZooTeam team = createTeam(leader);
        given(zooTeamRepository.findById(TEAM_ID)).willReturn(Optional.of(team));
        given(zooTeamMemberRepository.findByTeamIdWithUser(TEAM_ID)).willReturn(List.of(new ZooTeamMember(team, leader)));

        // when
        zooTeamService.getTeam(principalOf(master), TEAM_ID);

        // then
        then(zooTeamMissionRepository).should().findByTeamIdWithPhotoAndAnswers(TEAM_ID);
    }

    @Test
    @DisplayName("MASTER가 아니면 전체 제출 내용을 볼 수 없다")
    void getMissionResults_notMaster() {
        // when & then
        assertThatThrownBy(() -> zooTeamService.getMissionResults(principalOf(leader)))
                .isInstanceOf(ForbiddenException.class)
                .hasFieldOrPropertyWithValue("code", ZOO_ADMIN_ONLY.getCode());
    }

    private ZooMissionSubmitRequest missionRequest(Long photoFileId, String answer) {
        return new ZooMissionSubmitRequest(List.of(new ZooMissionSubmitRequest.AnswerRequest("q1", answer)), photoFileId);
    }

    private UploadedFile createPhoto(Long id, Long uploadedBy) {
        UploadedFile photo = new UploadedFile("zoo-mission.webp", "stored.webp", "uploads/stored.webp", "image/webp", 1024L);
        ReflectionTestUtils.setField(photo, "id", id);
        ReflectionTestUtils.setField(photo, "createdBy", uploadedBy);
        return photo;
    }

    private ZooTeam createTeam(User leader) {
        ZooTeam team = new ZooTeam("사자팀", ZooCourse.A, leader);
        ReflectionTestUtils.setField(team, "id", TEAM_ID);
        return team;
    }

    private UserPrincipal principalOf(User user) {
        return new UserPrincipal(user, null);
    }

    private User createUser(Long id, String name, Role role) {
        User user = new User("user" + id + "@test.com", name, AuthProvider.KAKAO, "provider_" + id);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }
}
