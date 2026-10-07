package com.gntcyouthbe.zoo.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import com.gntcyouthbe.zoo.domain.ZooPhotoVote;
import com.gntcyouthbe.zoo.domain.ZooTeamMission;
import com.gntcyouthbe.zoo.model.response.ZooPhotoListResponse;
import com.gntcyouthbe.zoo.model.response.ZooPhotoResponse;
import com.gntcyouthbe.zoo.model.response.ZooPhotoResultResponse;
import com.gntcyouthbe.zoo.repository.ZooPhotoVoteRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMemberRepository;
import com.gntcyouthbe.zoo.repository.ZooTeamMissionRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_ADMIN_ONLY;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_OWN_TEAM_PHOTO;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.ZOO_PHOTO_NOT_FOUND;

// 미션 사진 투표. 사진 id는 미션 id다. 권한 오류는 401로 바뀌지 않도록 @PreAuthorize가 아니라 여기서 403으로 던진다.
@Service
@RequiredArgsConstructor
public class ZooPhotoService {

    private final ZooTeamMissionRepository zooTeamMissionRepository;
    private final ZooTeamMemberRepository zooTeamMemberRepository;
    private final ZooPhotoVoteRepository zooPhotoVoteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ZooPhotoListResponse getPhotos(UserPrincipal userPrincipal) {
        Long myTeamId = zooTeamMemberRepository.findTeamIdByUserId(userPrincipal.getUserId()).orElse(null);
        Set<Long> votedPhotoIds = new HashSet<>(zooPhotoVoteRepository.findMissionIdsByUserId(userPrincipal.getUserId()));

        List<ZooPhotoResponse> photos = zooTeamMissionRepository.findAllWithTeamAndPhoto().stream()
                .map(mission -> ZooPhotoResponse.of(
                        mission,
                        mission.getTeam().getId().equals(myTeamId),
                        votedPhotoIds.contains(mission.getId())))
                .toList();
        return new ZooPhotoListResponse(photos);
    }

    // 사진(미션) 행을 잠근 뒤 확인하고 저장해서, 두 번 탭하거나 동시에 들어와도 표는 하나만 생긴다
    @Transactional
    public void vote(UserPrincipal userPrincipal, Long photoId) {
        ZooTeamMission mission = zooTeamMissionRepository.findByIdForUpdate(photoId)
                .orElseThrow(() -> new EntityNotFoundException(ZOO_PHOTO_NOT_FOUND));
        Long userId = userPrincipal.getUserId();

        // 여러 장에 투표할 수 있어서, 우리 조 사진을 막지 않으면 인원 많은 조가 유리해진다
        if (zooTeamMemberRepository.existsByTeamIdAndUserId(mission.getTeam().getId(), userId)) {
            throw new ForbiddenException(ZOO_OWN_TEAM_PHOTO);
        }
        if (zooPhotoVoteRepository.existsByMissionIdAndUserId(photoId, userId)) {
            return;
        }

        zooPhotoVoteRepository.save(new ZooPhotoVote(mission, findUser(userId)));
    }

    // 출발한 조는 조원이 바뀌지 않아 우리 조 사진에 표가 생길 수 없으므로, 취소할 때는 우리 조인지 보지 않는다
    @Transactional
    public void cancelVote(UserPrincipal userPrincipal, Long photoId) {
        if (!zooTeamMissionRepository.existsById(photoId)) {
            throw new EntityNotFoundException(ZOO_PHOTO_NOT_FOUND);
        }
        zooPhotoVoteRepository.deleteByMissionIdAndUserId(photoId, userPrincipal.getUserId());
    }

    @Transactional(readOnly = true)
    public ZooPhotoResultResponse getResults(UserPrincipal userPrincipal) {
        if (userPrincipal.getRole() != Role.MASTER) {
            throw new ForbiddenException(ZOO_ADMIN_ONLY);
        }

        Map<Long, Long> voteCounts = zooPhotoVoteRepository.countGroupByMissionId().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        List<ZooPhotoResultResponse.PhotoResult> photos = zooTeamMissionRepository.findAllWithTeamAndPhoto().stream()
                .map(mission -> ZooPhotoResultResponse.PhotoResult.of(mission, voteCounts.getOrDefault(mission.getId(), 0L)))
                .sorted(Comparator.comparingLong(ZooPhotoResultResponse.PhotoResult::voteCount).reversed()
                        .thenComparing(ZooPhotoResultResponse.PhotoResult::id))
                .toList();
        return new ZooPhotoResultResponse(zooPhotoVoteRepository.countDistinctVoters(), photos);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }
}
