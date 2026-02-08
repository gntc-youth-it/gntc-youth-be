package com.gntcyouthbe.user.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.request.UserProfileRequest;
import com.gntcyouthbe.user.model.response.UserProfileResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_PROFILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(final UserPrincipal userPrincipal) {
        final UserProfile profile = getUserProfile(userPrincipal.getUserId());
        return UserProfileResponse.from(profile);
    }

    @Transactional
    public UserProfileResponse saveProfile(final UserPrincipal userPrincipal, final UserProfileRequest request) {
        final var existing = userProfileRepository.findByUserId(userPrincipal.getUserId());

        if (existing.isPresent()) {
            final UserProfile profile = existing.get();
            profile.update(request.getGeneration(), request.getPhoneNumber(), request.getGender());
            return UserProfileResponse.from(profile);
        }

        final User user = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        final UserProfile profile = userProfileRepository.save(
                new UserProfile(user, request.getGeneration(), request.getPhoneNumber(), request.getGender())
        );
        return UserProfileResponse.from(profile);
    }

    private UserProfile getUserProfile(final Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_PROFILE_NOT_FOUND));
    }
}
