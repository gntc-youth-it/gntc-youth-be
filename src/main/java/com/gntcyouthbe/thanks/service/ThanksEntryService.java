package com.gntcyouthbe.thanks.service;

import com.gntcyouthbe.common.exception.BadRequestException;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.file.service.FileStorageService;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksMediaType;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.thanks.model.request.ThanksEntryCreateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksEntryListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ALREADY_POSTED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_FILE_NOT_ATTACHABLE;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_SLOT_CLOSED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ThanksEntryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ThanksEntryRepository thanksEntryRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final FileStorageService fileStorageService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ThanksEntryListResponse getEntries(UserPrincipal userPrincipal, LocalDate date) {
        List<ThanksEntry> entries = thanksEntryRepository.findByMissionDateWithUserAndFile(date);

        List<Long> authorIds = entries.stream().map(entry -> entry.getUser().getId()).distinct().toList();
        Map<Long, String> profileImagePaths = findProfileImagePaths(authorIds);

        List<ThanksEntryResponse> responses = entries.stream()
                .map(entry -> ThanksEntryResponse.of(
                        entry,
                        profileImagePaths.get(entry.getUser().getId()),
                        userPrincipal.getUserId()))
                .toList();
        return new ThanksEntryListResponse(date, responses);
    }

    // 날짜와 칸은 요청을 받은 시각으로 서버가 정한다. 그래서 지난 칸을 나중에 채울 수 없다.
    // 운영 JVM의 기본 시간대가 UTC라서 한국 시각으로 바꿔서 정한다.
    @Transactional
    public ThanksEntryResponse createEntry(UserPrincipal userPrincipal, ThanksEntryCreateRequest request) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), KST);
        ThanksSlot slot = ThanksSlot.openAt(now.toLocalTime())
                .orElseThrow(() -> new BadRequestException(THANKS_SLOT_CLOSED));
        LocalDate missionDate = now.toLocalDate();
        Long userId = userPrincipal.getUserId();

        if (thanksEntryRepository.existsByUserIdAndMissionDateAndSlot(userId, missionDate, slot)) {
            throw new BadRequestException(THANKS_ALREADY_POSTED);
        }
        UploadedFile file = findAttachableFile(userId, request.fileId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        ThanksEntry entry = save(new ThanksEntry(user, missionDate, slot, request.content(), file));
        return ThanksEntryResponse.of(entry, findProfileImagePaths(List.of(userId)).get(userId), userId);
    }

    // 갤러리 게시글을 지울 때처럼 붙인 파일의 기록과 S3 파일도 함께 지운다
    @Transactional
    public void deleteEntry(UserPrincipal userPrincipal, Long entryId) {
        ThanksEntry entry = thanksEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException(THANKS_ENTRY_NOT_FOUND));
        // 권한 오류는 401로 바뀌지 않도록 @PreAuthorize가 아니라 여기서 403으로 던진다
        if (!entry.isWrittenBy(userPrincipal.getUserId()) && userPrincipal.getRole() != Role.MASTER) {
            throw new ForbiddenException(THANKS_ENTRY_ACCESS_DENIED);
        }

        UploadedFile file = entry.getFile();
        String filePath = file.getFilePath();
        thanksEntryRepository.delete(entry);
        uploadedFileRepository.delete(file);
        fileStorageService.deleteFiles(List.of(filePath));
    }

    // 같은 칸에 동시에 두 번 올리면 unique 제약에 걸린다. IDENTITY라 INSERT가 바로 실행되므로 여기서 잡을 수 있고, 트랜잭션은 롤백된다.
    // 같은 파일이면 두 unique 제약(날짜·칸, 파일)에 함께 걸리는데, DB가 어느 쪽을 알려 줄지는 정해져 있지 않다.
    // 파일은 올린 사람만 붙이고 칸은 서버 시각으로 정하므로 어느 쪽이든 같은 칸의 중복 요청이라 6002로 본다.
    // NOT NULL 같은 다른 무결성 오류는 버그이므로 그대로 던진다.
    private ThanksEntry save(ThanksEntry entry) {
        try {
            return thanksEntryRepository.saveAndFlush(entry);
        } catch (DataIntegrityViolationException e) {
            if (isUniqueViolation(e)) {
                throw new BadRequestException(THANKS_ALREADY_POSTED);
            }
            throw e;
        }
    }

    // Spring은 unique 위반을 DuplicateKeyException이 아니라, Hibernate 예외를 감싼 DataIntegrityViolationException으로 던진다
    private boolean isUniqueViolation(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException violation
                && violation.getKind() == ConstraintKind.UNIQUE;
    }

    // 다른 사람이 올린 파일을 붙여 그 경로를 알아내지 못하게 본인이 올린 파일만 받는다.
    // 감사를 지우면 파일도 지우므로, 이미 다른 감사에 붙은 파일도 받지 않는다.
    private UploadedFile findAttachableFile(Long userId, Long fileId) {
        UploadedFile file = Optional.ofNullable(fileId)
                .flatMap(uploadedFileRepository::findById)
                .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));

        boolean isOwnUpload = userId.equals(file.getCreatedBy());
        boolean isMedia = ThanksMediaType.from(file.getContentType()).isPresent();
        if (!isOwnUpload || !isMedia || thanksEntryRepository.existsByFileId(fileId)) {
            throw new BadRequestException(THANKS_FILE_NOT_ATTACHABLE);
        }
        return file;
    }

    private Map<Long, String> findProfileImagePaths(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userProfileRepository.findByUserIdInWithProfileImage(userIds).stream()
                .filter(profile -> profile.getProfileImage() != null)
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        profile -> profile.getProfileImage().getFilePath()
                ));
    }
}
