package com.gntcyouthbe.zoo.service;

import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import com.gntcyouthbe.zoo.domain.ZooStop;
import com.gntcyouthbe.zoo.domain.ZooTeam;
import com.gntcyouthbe.zoo.domain.ZooTeamArrival;
import com.gntcyouthbe.zoo.domain.ZooTeamMember;
import com.gntcyouthbe.zoo.model.request.ZooTeamCourseUpdateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamCreateRequest;
import com.gntcyouthbe.zoo.model.request.ZooTeamLeaderTransferRequest;
import com.gntcyouthbe.zoo.model.response.MyZooTeamResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamDetailResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamListResponse;
import com.gntcyouthbe.zoo.model.response.ZooTeamSummaryResponse;
import com.gntcyouthbe.zoo.repository.ZooTeamArrivalRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMemberRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
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
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

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
                .map(member -> buildDetailResponse(member.getTeam()))
                .orElse(null);
        return new MyZooTeamResponse(team);
    }

    @Transactional(readOnly = true)
    public ZooTeamDetailResponse getTeam(Long teamId) {
        ZooTeam team = zooTeamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ZOO_TEAM_NOT_FOUND));
        return buildDetailResponse(team);
    }

    @Transactional
    public ZooTeamDetailResponse createTeam(UserPrincipal userPrincipal, ZooTeamCreateRequest request) {
        if (zooTeamMemberRepository.existsByUserId(userPrincipal.getUserId())) {
            throw new BadRequestException(ZOO_ALREADY_IN_TEAM);
        }
        User leader = findUser(userPrincipal.getUserId());

        ZooTeam team = zooTeamRepository.save(new ZooTeam(request.name(), request.course(), leader));
        addMember(team, leader);

        return buildDetailResponse(team);
    }

    @Transactional
    public ZooTeamDetailResponse joinTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = findTeamForUpdate(teamId);
        Long userId = userPrincipal.getUserId();

        if (zooTeamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            return buildDetailResponse(team);
        }
        validateNotStarted(team);
        if (zooTeamMemberRepository.existsByUserId(userId)) {
            throw new BadRequestException(ZOO_ALREADY_IN_TEAM);
        }

        addMember(team, findUser(userId));
        return buildDetailResponse(team);
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
        return buildDetailResponse(team);
    }

    @Transactional
    public ZooTeamDetailResponse startTeam(UserPrincipal userPrincipal, Long teamId) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);

        team.start();
        return buildDetailResponse(team);
    }

    // 코스 순서는 검사하지 않는다. 문 닫은 곳을 건너뛰고 아무 장소나 기록할 수 있다.
    @Transactional
    public ZooTeamDetailResponse markArrival(UserPrincipal userPrincipal, Long teamId, ZooStop stop) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        validateStarted(team);

        if (!zooTeamArrivalRepository.existsByTeamIdAndStop(teamId, stop)) {
            zooTeamArrivalRepository.save(new ZooTeamArrival(team, stop));
        }
        return buildDetailResponse(team);
    }

    @Transactional
    public ZooTeamDetailResponse cancelArrival(UserPrincipal userPrincipal, Long teamId, ZooStop stop) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);
        validateStarted(team);

        zooTeamArrivalRepository.deleteByTeamIdAndStop(teamId, stop);
        return buildDetailResponse(team);
    }

    @Transactional
    public ZooTeamDetailResponse transferLeader(UserPrincipal userPrincipal, Long teamId,
            ZooTeamLeaderTransferRequest request) {
        ZooTeam team = findTeamForUpdate(teamId);
        validateLeaderAccess(userPrincipal, team);

        ZooTeamMember newLeader = zooTeamMemberRepository.findByTeamIdAndUserId(teamId, request.userId())
                .orElseThrow(() -> new BadRequestException(ZOO_NOT_TEAM_MEMBER));
        team.changeLeader(newLeader.getUser());

        return buildDetailResponse(team);
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

    private ZooTeamDetailResponse buildDetailResponse(ZooTeam team) {
        List<ZooTeamMember> members = zooTeamMemberRepository.findByTeamIdWithUser(team.getId());

        List<Long> userIds = members.stream().map(member -> member.getUser().getId()).toList();
        Map<Long, String> profileImagePaths = userProfileRepository.findByUserIdInWithProfileImage(userIds).stream()
                .filter(profile -> profile.getProfileImage() != null)
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        profile -> profile.getProfileImage().getFilePath()
                ));

        List<ZooTeamArrival> arrivals = zooTeamArrivalRepository.findByTeamIdOrderByIdAsc(team.getId());

        return ZooTeamDetailResponse.of(team, members, profileImagePaths, arrivals);
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
