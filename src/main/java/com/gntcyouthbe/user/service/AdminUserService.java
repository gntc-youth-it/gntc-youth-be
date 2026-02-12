package com.gntcyouthbe.user.service;

import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.response.AdminUserListResponse;
import com.gntcyouthbe.user.model.response.AdminUserResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers(int page, int size, String name) {
        PageRequest pageRequest = PageRequest.of(page, size);
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
}
