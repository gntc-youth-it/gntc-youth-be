package com.gntcyouthbe.user.service;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.repository.ChurchRepository;
import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.model.ExceptionCode;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.request.UserRoleUpdateRequest;
import com.gntcyouthbe.user.model.response.AdminUserListResponse;
import com.gntcyouthbe.user.model.response.AdminUserResponse;
import com.gntcyouthbe.user.model.response.ChurchLeaderResponse;
import com.gntcyouthbe.user.model.response.UserRoleUpdateResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "id");

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ChurchRepository churchRepository;

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers(int page, int size, String name) {
        PageRequest pageRequest = PageRequest.of(page, size, DEFAULT_SORT);
        Page<User> userPage = StringUtils.hasText(name)
                ? userRepository.findAllWithChurchByNameContaining(name, pageRequest)
                : userRepository.findAllWithChurch(pageRequest);
        List<Long> userIds = userPage.getContent().stream().map(User::getId).toList();
        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));

        List<AdminUserResponse> responses = userPage.getContent().stream()
                .map(user -> AdminUserResponse.from(user, profileMap.get(user.getId())))
                .toList();

        return new AdminUserListResponse(responses, userPage.getTotalElements(),
                userPage.getTotalPages(), page, size);
    }

    @Transactional(readOnly = true)
    public ChurchLeaderResponse getChurchLeader(ChurchId churchId) {
        if (!churchRepository.existsById(churchId)) {
            throw new EntityNotFoundException(ExceptionCode.CHURCH_NOT_FOUND);
        }

        User leader = userRepository.findLeaderByChurchId(churchId).orElse(null);
        return ChurchLeaderResponse.of(churchId, leader);
    }

    @Transactional
    public UserRoleUpdateResponse updateUserRole(Long userId, UserRoleUpdateRequest request) {
        Role newRole = request.getRole();
        if (newRole != Role.LEADER && newRole != Role.USER) {
            throw new BadRequestException(ExceptionCode.INVALID_ROLE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.USER_NOT_FOUND));

        if (user.getRole() == newRole) {
            throw new BadRequestException(ExceptionCode.SAME_ROLE);
        }

        Optional<User> demotedLeader = Optional.empty();

        if (newRole == Role.LEADER) {
            if (user.getChurchId() == null) {
                throw new BadRequestException(ExceptionCode.USER_NO_CHURCH);
            }

            demotedLeader = userRepository.findLeaderByChurchId(user.getChurchId());
            demotedLeader.ifPresent(leader -> leader.updateRole(Role.USER));
        }

        user.updateRole(newRole);

        return UserRoleUpdateResponse.of(user, demotedLeader.orElse(null));
    }
}
