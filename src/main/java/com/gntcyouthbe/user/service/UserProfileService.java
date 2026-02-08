package com.gntcyouthbe.user.service;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.repository.ChurchRepository;
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
    private final ChurchRepository churchRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(final UserPrincipal userPrincipal) {
        final User user = getUser(userPrincipal);
        final UserProfile profile = getUserProfile(userPrincipal);
        return UserProfileResponse.from(user, profile);
    }

    @Transactional
    public UserProfileResponse saveProfile(final UserPrincipal userPrincipal, final UserProfileRequest request) {
        final User user = getUser(userPrincipal);
        final Church church = churchRepository.getReferenceById(request.getChurchId());
        user.updateNameAndChurch(request.getName(), church);

        final UserProfile profile = userProfileRepository.findByUserId(userPrincipal.getUserId())
                .map(existing -> {
                    existing.update(request);
                    return existing;
                })
                .orElseGet(() -> new UserProfile(user, request.getGeneration(), request.getPhoneNumber(), request.getGender()));

        userProfileRepository.save(profile);
        return UserProfileResponse.from(user, profile);
    }

    private User getUser(final UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }

    private UserProfile getUserProfile(final UserPrincipal userPrincipal) {
        return userProfileRepository.findByUserId(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_PROFILE_NOT_FOUND));
    }
}
