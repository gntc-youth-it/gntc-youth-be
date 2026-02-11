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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public AdminUserListResponse getAllUsers() {
        List<User> users = userRepository.findAllWithChurch();
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));

        List<AdminUserResponse> responses = users.stream()
                .map(user -> AdminUserResponse.from(user, profileMap.get(user.getId())))
                .toList();

        return new AdminUserListResponse(responses);
    }
}
