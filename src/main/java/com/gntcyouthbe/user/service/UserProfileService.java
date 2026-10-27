package com.gntcyouthbe.user.service;

import com.gntcyouthbe.church.domain.Church;
import com.gntcyouthbe.church.repository.ChurchRepository;
import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.domain.UserProfile;
import com.gntcyouthbe.user.model.request.UserProfilePatchRequest;
import com.gntcyouthbe.user.model.request.UserProfileRequest;
import com.gntcyouthbe.user.model.response.UserProfileResponse;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.PROFILE_IMAGE_NOT_ATTACHABLE;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;


@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final UploadedFileRepository uploadedFileRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(final UserPrincipal userPrincipal) {
        final User user = getUser(userPrincipal);
        final UserProfile profile = userProfileRepository.findByUserId(userPrincipal.getUserId())
                .orElse(null);
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

        final UploadedFile profileImage = resolveProfileImage(request.getProfileImageId());
        profile.updateProfileImage(profileImage);

        userProfileRepository.save(profile);
        return UserProfileResponse.from(user, profile);
    }

    // 보낸 것만 바꾼다. 사진을 보냈는데 프로필 행이 없으면 사진만 채운 행을 새로 만든다.
    // 바꾸기 전 사진 파일은 지우지 않는다. 같은 파일을 다른 곳에서도 가리키고 있으면 그쪽 사진이 깨진다.
    @Transactional
    public UserProfileResponse updateProfile(final UserPrincipal userPrincipal, final UserProfilePatchRequest request) {
        final User user = getUser(userPrincipal);
        UserProfile profile = userProfileRepository.findByUserId(userPrincipal.getUserId()).orElse(null);

        if (request.profileImageId() != null) {
            final UploadedFile profileImage = findAttachableProfileImage(
                    userPrincipal.getUserId(), request.profileImageId(), profile);
            if (profile == null) {
                profile = new UserProfile(user);
            }
            profile.updateProfileImage(profileImage);
            userProfileRepository.save(profile);
        }
        if (request.name() != null) {
            user.updateName(request.name());
        }
        return UserProfileResponse.from(user, profile);
    }

    // 다른 사람이 올린 파일을 붙여 그 경로를 알아내지 못하게, 본인이 올린 이미지만 받는다.
    // 지금 쓰는 사진을 다시 보내면 그대로 둔다. PUT으로 붙인 사진은 올린 사람을 확인하지 않았기 때문이다.
    private UploadedFile findAttachableProfileImage(Long userId, Long profileImageId, UserProfile profile) {
        final UploadedFile image = uploadedFileRepository.findById(profileImageId)
                .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));

        final boolean isCurrentImage = profile != null && profile.getProfileImage() != null
                && profile.getProfileImage().getId().equals(profileImageId);
        if (!isCurrentImage && (!image.isUploadedBy(userId) || !image.isImage())) {
            throw new BadRequestException(PROFILE_IMAGE_NOT_ATTACHABLE);
        }
        return image;
    }

    private UploadedFile resolveProfileImage(Long profileImageId) {
        if (profileImageId == null) {
            return null;
        }
        return uploadedFileRepository.findById(profileImageId)
                .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));
    }

    private User getUser(final UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
    }
}
