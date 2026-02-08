package com.gntcyouthbe.church.service;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.domain.ChurchInfo;
import com.gntcyouthbe.church.domain.PrayerTopic;
import com.gntcyouthbe.church.model.request.ChurchInfoRequest;
import com.gntcyouthbe.church.model.request.PrayerTopicRequest;
import com.gntcyouthbe.church.model.response.ChurchInfoResponse;
import com.gntcyouthbe.church.repository.ChurchInfoRepository;
import com.gntcyouthbe.church.repository.PrayerTopicRepository;
import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChurchInfoServiceTest {

    @Mock
    private ChurchInfoRepository churchInfoRepository;

    @Mock
    private PrayerTopicRepository prayerTopicRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private ChurchInfoService churchInfoService;

    @Test
    @DisplayName("성전 정보 신규 저장 성공")
    void saveChurchInfo_create() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);
        UploadedFile file = new UploadedFile("photo.jpg", "stored.jpg", "/uploads/stored.jpg", "image/jpeg", 1024L);
        ChurchInfoRequest request = new ChurchInfoRequest(1L, List.of(
                new PrayerTopicRequest("교회의 부흥을 위해", 1),
                new PrayerTopicRequest("청년들의 신앙 성장을 위해", 2)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.empty());
        given(churchInfoRepository.save(any(ChurchInfo.class))).willReturn(churchInfo);
        given(uploadedFileRepository.findById(1L)).willReturn(Optional.of(file));
        given(prayerTopicRepository.findByChurchInfoOrderBySortOrderAsc(churchInfo)).willReturn(Collections.emptyList());
        given(prayerTopicRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ChurchInfoResponse response = churchInfoService.saveChurchInfo(ChurchId.ANYANG, request);

        // then
        assertThat(response.getChurchId()).isEqualTo("ANYANG");
        assertThat(response.getPrayerTopics()).hasSize(2);
        assertThat(response.getPrayerTopics().get(0).getContent()).isEqualTo("교회의 부흥을 위해");
        assertThat(response.getPrayerTopics().get(1).getContent()).isEqualTo("청년들의 신앙 성장을 위해");
    }

    @Test
    @DisplayName("성전 정보 수정 성공 - 기존 기도제목 교체")
    void saveChurchInfo_update() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.SUWON);
        PrayerTopic existingTopic = new PrayerTopic(churchInfo, "기존 기도제목", 1);
        ChurchInfoRequest request = new ChurchInfoRequest(null, List.of(
                new PrayerTopicRequest("수정된 기도제목", 1)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.SUWON)).willReturn(Optional.of(churchInfo));
        given(prayerTopicRepository.findByChurchInfoOrderBySortOrderAsc(churchInfo)).willReturn(List.of(existingTopic));
        given(prayerTopicRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ChurchInfoResponse response = churchInfoService.saveChurchInfo(ChurchId.SUWON, request);

        // then
        assertThat(response.getPrayerTopics()).hasSize(1);
        assertThat(response.getPrayerTopics().get(0).getContent()).isEqualTo("수정된 기도제목");
        verify(prayerTopicRepository).deleteAll(List.of(existingTopic));
    }

    @Test
    @DisplayName("성전 정보 저장 실패 - 존재하지 않는 파일")
    void saveChurchInfo_fileNotFound() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);
        ChurchInfoRequest request = new ChurchInfoRequest(999L, List.of(
                new PrayerTopicRequest("기도제목", 1)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.of(churchInfo));
        given(uploadedFileRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> churchInfoService.saveChurchInfo(ChurchId.ANYANG, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("성전 정보 조회 성공")
    void getChurchInfo_success() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);
        List<PrayerTopic> prayerTopics = List.of(
                new PrayerTopic(churchInfo, "기도제목1", 1),
                new PrayerTopic(churchInfo, "기도제목2", 2)
        );

        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.of(churchInfo));
        given(prayerTopicRepository.findByChurchInfoOrderBySortOrderAsc(churchInfo)).willReturn(prayerTopics);

        // when
        ChurchInfoResponse response = churchInfoService.getChurchInfo(ChurchId.ANYANG);

        // then
        assertThat(response.getChurchId()).isEqualTo("ANYANG");
        assertThat(response.getPrayerTopics()).hasSize(2);
    }

    @Test
    @DisplayName("성전 정보 조회 실패 - 존재하지 않는 성전 정보")
    void getChurchInfo_notFound() {
        // given
        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> churchInfoService.getChurchInfo(ChurchId.ANYANG))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
