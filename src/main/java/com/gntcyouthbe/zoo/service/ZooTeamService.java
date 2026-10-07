package com.gntcyouthbe.zoo.service;

import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import com.gntcyouthbe.zoo.domain.ZooMissionAnswer;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamArrival;
import com.gntcyouthbe.zoo.domain.ZooTeamMember;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import com.gntcyouthbe.zoo.model.request.ZooMissionSubmitRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamCourseUpdateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamCreateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamLeaderTransferRequest;
import com.gntcyouthbe.zoo.model.response.MyZooTeamResponse;
import com.gntcyouthbe.zoo.model.response.ZooMissionResultResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamDetailResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamListResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamSummaryResponse;
import com.gntcyouthbe.zoo.repository.ZooTeamArrivalRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMemberRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_ADMIN_ONLY;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_ALREADY_IN_TEAM;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_LEADER_CANNOT_LEAVE;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_NOT_TEAM_LEADER;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_NOT_TEAM_MEMBER;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_TEAM_ALREADY_STARTED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_TEAM_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_TEAM_NOT_STARTED;

// 조를 바꾸는 요청은 모두 findTeamForUpdate로 조 행을 잠근 뒤 확인하고 저장한다. 새 변경 API도 이 규칙을 따라야 한다.
@Service
@RequiredArgsConstructor
public class ZooTeamService {

    private final ZooTeamRepository zooTeamRepository;
    private final ZooTeamMemberRepository zooTeamMemberRepository;
    private final ZooTeamArrivalRepository zooTeamArrivalRepository;
    private final ZooTeamMissionRepository zooTeamMissionRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UploadedFileRepository uploadedFileRepository;

    @Transactional(readOnly = true)
    public ZooTeamListResponse getTeams() {
        Map<Long, Long> memberCounts = toCountMap(zooTeamMemberRepository.countGroupByTeamId());
        Map<Long, Long> arrivedCounts = toCountMap(zooTeamArrivalRepository.countGroupByTeamId());

        List<ZooTeamSummaryResponse> teams = zooTeamRepository.findAllWithLeader().stream()
                .map(team -> ZooTeamSummaryResponse.of(
                        team,
                        memberCounts.getOrDefault(team.getId(), 0L),
                        arrivedCounts.getOrDefault(team.getId(), 0L)))
                .toList();
        return new ZooTeamListResponse(teams);
    }

    @Transactional(readOnly = true)
    public MyZooTeamResponse getMyTeam(UserPrincipal userPrincipal) {
        ZooTeamDetailResponse team = zooTeamMemberRepository.findByUserIdWithTeam(userPrincipal.getUserId())
                .map(member -> buildDetailResponse(member.getTeam(), userPrincipal))
                .orElse(null);
        return new MyZooTeamResponse(team);
    }

    @Transactional(readOnly = true)
    public ZooTeamDetailResponse getTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = zooTeamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ZOO_TEAM_NOT_FOUND));
        return buildDetailResponse(team, userPrincipal);
    }

    @Transactional
    public ZooTeamDetailResponse createTeam(UserPrincipal userPrincipal, ZooTeamCreateRequest request) {
        if (zooTeamMemberRepository.existsByUserId(userPrincipal.getUserId())) {
            throw new BadRequestException(ZOO_ALREADY_IN_TEAM);
        }
        User leader = findUser(userPrincipal.getUserId());

        ZooTeam team = zooTeamRepository.save(new ZooTeam(request.name(), request.course(), leader));
        addMember(team, leader);

        return buildDetailResponse(team, userPrincipal);
    }

    @Transactional
    public ZooTeamDetailResponse joinTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = findTeamForUpdate(teamId);
        Long userId = userPrincipal.getUserId();

        if (zooTeamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            return buildDetailResponse(team, userPrincipal);
        }
        validateNotStarted(team);
        if (zooTeamMemberRepository.existsByUserId(userId)) {
            throw new BadRequestException(ZOO_ALREADY_IN_TEAM);
        }

        addMember(team, findUser(userId));
        return buildDetailResponse(team, userPrincipal);
    }

    @Transactional
    public void leaveTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = findTeamForUpdate(teamId);
        ZooTeamMember member = zooTeamMemberRepository.findByTeamIdAndUserId(teamId, userPrincipal.getUserId())
                .orElseThrow(() -> new BadRequestException(ZOO_NOT_TEAM_MEMBER));

        if (team.isLeader(userPrincipal.getUserId())) {
            throw new BadRequestException(ZOO_LEADER_CANNOT_LEAVE);
        }
        validateNotStarted(team);

        zooTeamMemberRepository.delete(member);
    }

    @Transactional
    public void deleteTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        // 출발한 조는 조장이 지울 수 없다. 리허설 데이터 정리처럼 꼭 지워야 할 때를 위해 MASTER는 지울 수 있다.
        if (!isMaster(userPrincipal)) {
            validateNotStarted(team);
        }

        zooTeamMissionRepository.deleteAll(zooTeamMissionRepository.findByTeamId(teamId));
        zooTeamArrivalRepository.deleteByTeamId(teamId);
        zooTeamMemberRepository.deleteByTeamId(teamId);
        zooTeamRepository.delete(team);
    }

    @Transactional
    public ZooTeamDetailResponse changeCourse(UserPrincipal userPrincipal, Long teamId,
            ZooTeamCourseUpdateRequest request) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        validateNotStarted(team);

        team.changeCourse(request.course());
        return buildDetailResponse(team, userPrincipal);
    }

    @Transactional
    public ZooTeamDetailResponse startTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);

        team.start();
        return buildDetailResponse(team, userPrincipal);
    }

    // 코스 순서는 검사하지 않는다. 문 닫은 곳을 건너뛰고 아무 장소나 기록할 수 있다.
    @Transactional
    public ZooTeamDetailResponse markArrival(UserPrincipal userPrincipal, Long teamId, ZooStop stop) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        validateStarted(team);

        recordArrival(team, stop);
        return buildDetailResponse(team, userPrincipal);
    }

    @Transactional
    public ZooTeamDetailResponse cancelArrival(UserPrincipal userPrincipal, Long teamId, ZooStop stop) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        validateStarted(team);

        zooTeamArrivalRepository.deleteByTeamIdAndStop(teamId, stop);
        return buildDetailResponse(team, userPrincipal);
    }

    // 미션을 내면 그 장소가 도착 처리된다. 다시 내면 답과 사진을 바꾸고, 도착 시각과 처음 제출 시각은 그대로 둔다.
    @Transactional
    public ZooTeamDetailResponse submitMission(UserPrincipal userPrincipal, Long teamId, ZooStop stop,
            ZooMissionSubmitRequest request) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        validateStarted(team);

        Optional<ZooTeamMission> submitted = zooTeamMissionRepository.findByTeamIdAndStop(teamId, stop);
        UploadedFile photo = findMissionPhoto(userPrincipal, request.photoFileId(), submitted.orElse(null));
        List<ZooMissionAnswer> answers = request.answers().stream()
                .map(answer -> new ZooMissionAnswer(answer.questionId(), answer.answer()))
                .toList();

        submitted.ifPresentOrElse(
                mission -> mission.resubmit(photo, answers),
                () -> zooTeamMissionRepository.save(new ZooTeamMission(team, stop, photo, answers))
        );
        recordArrival(team, stop);

        return buildDetailResponse(team, userPrincipal);
    }

    @Transactional
    public ZooTeamDetailResponse transferLeader(UserPrincipal userPrincipal, Long teamId,
            ZooTeamLeaderTransferRequest request) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);

        ZooTeamMember newLeader = zooTeamMemberRepository.findByTeamIdAndUserId(teamId, request.userId())
                .orElseThrow(() -> new BadRequestException(ZOO_NOT_TEAM_MEMBER));
        team.changeLeader(newLeader.getUser());

        return buildDetailResponse(team, userPrincipal);
    }

    // 운영자 권한 오류도 401로 바뀌지 않도록 @PreAuthorize가 아니라 여기서 403으로 던진다
    @Transactional(readOnly = true)
    public ZooMissionResultResponse getMissionResults(UserPrincipal userPrincipal) {
        if (!isMaster(userPrincipal)) {
            throw new ForbiddenException(ZOO_ADMIN_ONLY);
        }

        Map<Long, List<String>> memberNames = zooTeamMemberRepository.findAllWithUser().stream()
                .collect(Collectors.groupingBy(
                        member -> member.getTeam().getId(),
                        Collectors.mapping(member -> member.getUser().getName(), Collectors.toList())
                ));
        Map<Long, List<ZooTeamMission>> missions = zooTeamMissionRepository.findAllWithPhotoAndAnswers().stream()
                .collect(Collectors.groupingBy(mission -> mission.getTeam().getId()));

        List<ZooMissionResultResponse.TeamResult> teams = zooTeamRepository.findAllWithLeader().stream()
                .map(team -> ZooMissionResultResponse.TeamResult.of(
                        team,
                        memberNames.getOrDefault(team.getId(), List.of()),
                        missions.getOrDefault(team.getId(), List.of())))
                .toList();
        return new ZooMissionResultResponse(teams);
    }

    private ZooTeam findTeamForUpdate(Long teamId) {
        return zooTeamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ZOO_TEAM_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    // 만들기를 두 번 탭하거나 두 조에 동시에 들어가면 uk_zoo_team_member_user_id에 걸린다.
    // IDENTITY라 INSERT가 바로 실행되므로 여기서 잡을 수 있고, 트랜잭션은 롤백된다.
    private void addMember(ZooTeam team, User user) {
        try {
            zooTeamMemberRepository.saveAndFlush(new ZooTeamMember(team, user));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ZOO_ALREADY_IN_TEAM);
        }
    }

    // 이미 도착한 장소면 처음 도착 시각을 그대로 둔다
    private void recordArrival(ZooTeam team, ZooStop stop) {
        if (!zooTeamArrivalRepository.existsByTeamIdAndStop(team.getId(), stop)) {
            zooTeamArrivalRepository.save(new ZooTeamArrival(team, stop));
        }
    }

    // 다른 사람이 올린 파일을 미션 사진으로 붙여 그 경로를 알아내지 못하게 한다.
    // 본인이 올린 파일, 이 미션에 이미 붙은 사진(답만 고칠 때), MASTER 요청만 허용하고 나머지는 없는 파일로 본다.
    private UploadedFile findMissionPhoto(UserPrincipal userPrincipal, Long photoFileId, ZooTeamMission submitted) {
        UploadedFile photo = uploadedFileRepository.findById(photoFileId)
                .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));

        boolean isOwnUpload = userPrincipal.getUserId().equals(photo.getCreatedBy());
        boolean isCurrentPhoto = submitted != null && submitted.getPhoto().getId().equals(photoFileId);
        if (!isOwnUpload && !isCurrentPhoto && !isMaster(userPrincipal)) {
            throw new EntityNotFoundException(FILE_NOT_FOUND);
        }
        return photo;
    }

    // 조장 권한 오류는 403으로 보낸다. @PreAuthorize로 막으면 GlobalExceptionHandler가 401로 바꿔서 프론트가 세션 만료로 처리한다.
    private void validateLeaderAccess(UserPrincipal userPrincipal, ZooTeam team) {
        if (!team.isLeader(userPrincipal.getUserId()) && !isMaster(userPrincipal)) {
            throw new ForbiddenException(ZOO_NOT_TEAM_LEADER);
        }
    }

    private boolean isMaster(UserPrincipal userPrincipal) {
        return userPrincipal.getRole() == Role.MASTER;
    }

    private void validateNotStarted(ZooTeam team) {
        if (team.isStarted()) {
            throw new BadRequestException(ZOO_TEAM_ALREADY_STARTED);
        }
    }

    private void validateStarted(ZooTeam team) {
        if (!team.isStarted()) {
            throw new BadRequestException(ZOO_TEAM_NOT_STARTED);
        }
    }

    // 조 상세는 초대 링크로 누구나 볼 수 있으므로, 미션은 조원과 MASTER에게만 채워 보낸다
    private ZooTeamDetailResponse buildDetailResponse(ZooTeam team, UserPrincipal viewer) {
        List<ZooTeamMember> members = zooTeamMemberRepository.findByTeamIdWithUser(team.getId());

        List<Long> userIds = members.stream().map(member -> member.getUser().getId()).toList();
        Map<Long, String> profileImagePaths = userProfileRepository.findByUserIdInWithProfileImage(userIds).stream()
                .filter(profile -> profile.getProfileImage() != null)
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        profile -> profile.getProfileImage().getFilePath()
                ));

        List<ZooTeamArrival> arrivals = zooTeamArrivalRepository.findByTeamIdOrderByIdAsc(team.getId());

        boolean canSeeMissions = isMaster(viewer) || userIds.contains(viewer.getUserId());
        List<ZooTeamMission> missions = canSeeMissions
                ? zooTeamMissionRepository.findByTeamIdWithPhotoAndAnswers(team.getId())
                : List.of();

        return ZooTeamDetailResponse.of(team, members, profileImagePaths, arrivals, missions);
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
