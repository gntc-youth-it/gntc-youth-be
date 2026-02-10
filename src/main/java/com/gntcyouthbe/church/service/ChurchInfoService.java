package com.gntcyouthbe.church.service;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.domain.ChurchInfo;
import com.gntcyouthbe.church.domain.PrayerTopic;
import com.gntcyouthbe.church.model.request.ChurchInfoRequest;
import com.gntcyouthbe.church.model.response.ChurchInfoResponse;
import com.gntcyouthbe.church.repository.ChurchInfoRepository;
import com.gntcyouthbe.church.repository.PrayerTopicRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.user.domain.Role;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.CHURCH_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.CHURCH_INFO_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.FILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ChurchInfoService {

    private final ChurchInfoRepository churchInfoRepository;
    private final PrayerTopicRepository prayerTopicRepository;
    private final UploadedFileRepository uploadedFileRepository;

    @Transactional
    public ChurchInfoResponse saveChurchInfo(UserPrincipal userPrincipal, ChurchId churchId, ChurchInfoRequest request) {
        validateChurchAccess(userPrincipal, churchId);

        ChurchInfo churchInfo = churchInfoRepository.findByChurchId(churchId)
                .orElseGet(() -> churchInfoRepository.save(new ChurchInfo(churchId)));

        if (request.getGroupPhotoFileId() != null) {
            UploadedFile file = uploadedFileRepository.findById(request.getGroupPhotoFileId())
                    .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));
            churchInfo.updateGroupPhoto(file);
        }

        prayerTopicRepository.deleteByChurchInfo(churchInfo);

        List<PrayerTopic> prayerTopics = request.getPrayerTopics().stream()
                .map(req -> new PrayerTopic(churchInfo, req.getContent(), req.getSortOrder()))
                .toList();
        prayerTopicRepository.saveAll(prayerTopics);

        return ChurchInfoResponse.from(churchInfo, prayerTopics);
    }

    private void validateChurchAccess(UserPrincipal userPrincipal, ChurchId churchId) {
        if (userPrincipal.getRole() == Role.LEADER && !churchId.equals(userPrincipal.getChurch())) {
            throw new ForbiddenException(CHURCH_ACCESS_DENIED);
        }
    }

    @Transactional(readOnly = true)
    public ChurchInfoResponse getChurchInfo(ChurchId churchId) {
        ChurchInfo churchInfo = churchInfoRepository.findByChurchId(churchId)
                .orElseThrow(() -> new EntityNotFoundException(CHURCH_INFO_NOT_FOUND));

        List<PrayerTopic> prayerTopics = prayerTopicRepository
                .findByChurchInfoOrderBySortOrderAsc(churchInfo);

        return ChurchInfoResponse.from(churchInfo, prayerTopics);
    }
}
