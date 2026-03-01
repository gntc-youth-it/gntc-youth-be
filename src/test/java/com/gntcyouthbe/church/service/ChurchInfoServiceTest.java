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
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import com.gntcyouthbe.post.repository.PostImageRepository;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private PostImageRepository postImageRepository;

    @InjectMocks
    private ChurchInfoService churchInfoService;

    private UserPrincipal createPrincipal(Role role, ChurchId churchId) {
        return new UserPrincipal(1L, "test@example.com", "테스트", role, churchId, AuthProvider.KAKAO, null);
    }

    @Test
    @DisplayName("성전 정보 신규 저장 성공")
    void saveChurchInfo_create() {
        // given
        UserPrincipal principal = createPrincipal(Role.LEADER, ChurchId.ANYANG);
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);
        UploadedFile file = new UploadedFile("photo.jpg", "stored.jpg", "/uploads/stored.jpg", "image/jpeg", 1024L);
        ChurchInfoRequest request = new ChurchInfoRequest(1L, List.of(
                new PrayerTopicRequest("교회의 부흥을 위해", 1),
                new PrayerTopicRequest("청년들의 신앙 성장을 위해", 2)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.empty());
        given(churchInfoRepository.save(any(ChurchInfo.class))).willReturn(churchInfo);
        given(uploadedFileRepository.findById(1L)).willReturn(Optional.of(file));
        given(prayerTopicRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));
        given(postImageRepository.findRandomImagePathsByChurch("ANYANG")).willReturn(List.of("/uploads/img1.jpg"));

        // when
        ChurchInfoResponse response = churchInfoService.saveChurchInfo(principal, ChurchId.ANYANG, request);

        // then
        assertThat(response.getChurchId()).isEqualTo(ChurchId.ANYANG);
        assertThat(response.getGroupPhotoPath()).isEqualTo("/uploads/stored.jpg");
        assertThat(response.getPrayerTopics()).hasSize(2);
        assertThat(response.getPrayerTopics().get(0).getContent()).isEqualTo("교회의 부흥을 위해");
        assertThat(response.getPrayerTopics().get(1).getContent()).isEqualTo("청년들의 신앙 성장을 위해");
    }

    @Test
    @DisplayName("성전 정보 수정 성공 - 기존 기도제목 교체")
    void saveChurchInfo_update() {
        // given
        UserPrincipal principal = createPrincipal(Role.LEADER, ChurchId.SUWON);
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.SUWON);
        ChurchInfoRequest request = new ChurchInfoRequest(null, List.of(
                new PrayerTopicRequest("수정된 기도제목", 1)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.SUWON)).willReturn(Optional.of(churchInfo));
        given(prayerTopicRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));
        given(postImageRepository.findRandomImagePathsByChurch("SUWON")).willReturn(List.of());

        // when
        ChurchInfoResponse response = churchInfoService.saveChurchInfo(principal, ChurchId.SUWON, request);

        // then
        assertThat(response.getPrayerTopics()).hasSize(1);
        assertThat(response.getPrayerTopics().get(0).getContent()).isEqualTo("수정된 기도제목");
        verify(prayerTopicRepository).deleteByChurchInfo(churchInfo);
    }

    @Test
    @DisplayName("성전 정보 저장 실패 - 존재하지 않는 파일")
    void saveChurchInfo_fileNotFound() {
        // given
        UserPrincipal principal = createPrincipal(Role.LEADER, ChurchId.ANYANG);
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);
        ChurchInfoRequest request = new ChurchInfoRequest(999L, List.of(
                new PrayerTopicRequest("기도제목", 1)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.of(churchInfo));
        given(uploadedFileRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> churchInfoService.saveChurchInfo(principal, ChurchId.ANYANG, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("LEADER가 다른 성전 정보 수정 시 실패")
    void saveChurchInfo_leaderAccessDenied() {
        // given
        UserPrincipal principal = createPrincipal(Role.LEADER, ChurchId.ANYANG);
        ChurchInfoRequest request = new ChurchInfoRequest(null, List.of(
                new PrayerTopicRequest("기도제목", 1)
        ));

        // when & then
        assertThatThrownBy(() -> churchInfoService.saveChurchInfo(principal, ChurchId.SUWON, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("MASTER는 다른 성전 정보 수정 가능")
    void saveChurchInfo_masterCanAccessAnyChurch() {
        // given
        UserPrincipal principal = createPrincipal(Role.MASTER, ChurchId.ANYANG);
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.SUWON);
        ChurchInfoRequest request = new ChurchInfoRequest(null, List.of(
                new PrayerTopicRequest("기도제목", 1)
        ));

        given(churchInfoRepository.findByChurchId(ChurchId.SUWON)).willReturn(Optional.of(churchInfo));
        given(prayerTopicRepository.saveAll(anyList())).willAnswer(invocation -> invocation.getArgument(0));
        given(postImageRepository.findRandomImagePathsByChurch("SUWON")).willReturn(List.of());

        // when
        ChurchInfoResponse response = churchInfoService.saveChurchInfo(principal, ChurchId.SUWON, request);

        // then
        assertThat(response.getChurchId()).isEqualTo(ChurchId.SUWON);
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
        given(postImageRepository.findRandomImagePathsByChurch("ANYANG"))
                .willReturn(List.of("/uploads/img1.jpg", "/uploads/img2.jpg"));

        // when
        ChurchInfoResponse response = churchInfoService.getChurchInfo(ChurchId.ANYANG);

        // then
        assertThat(response.getChurchId()).isEqualTo(ChurchId.ANYANG);
        assertThat(response.getPrayerTopics()).hasSize(2);
        assertThat(response.getRandomPhotos()).containsExactly("/uploads/img1.jpg", "/uploads/img2.jpg");
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

    @Test
    @DisplayName("성전 정보 조회 시 랜덤 사진이 포함된다")
    void getChurchInfo_withRandomPhotos() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.ANYANG);
        List<PrayerTopic> prayerTopics = List.of(new PrayerTopic(churchInfo, "기도제목", 1));
        List<String> randomPhotos = List.of(
                "/uploads/img1.jpg", "/uploads/img2.jpg", "/uploads/img3.jpg",
                "/uploads/img4.jpg", "/uploads/img5.jpg", "/uploads/img6.jpg",
                "/uploads/img7.jpg"
        );

        given(churchInfoRepository.findByChurchId(ChurchId.ANYANG)).willReturn(Optional.of(churchInfo));
        given(prayerTopicRepository.findByChurchInfoOrderBySortOrderAsc(churchInfo)).willReturn(prayerTopics);
        given(postImageRepository.findRandomImagePathsByChurch("ANYANG")).willReturn(randomPhotos);

        // when
        ChurchInfoResponse response = churchInfoService.getChurchInfo(ChurchId.ANYANG);

        // then
        assertThat(response.getRandomPhotos()).hasSize(7);
        assertThat(response.getRandomPhotos()).containsExactlyElementsOf(randomPhotos);
    }

    @Test
    @DisplayName("성전에 사진이 없을 경우 빈 배열이 반환된다")
    void getChurchInfo_noPhotos() {
        // given
        ChurchInfo churchInfo = new ChurchInfo(ChurchId.SUWON);
        List<PrayerTopic> prayerTopics = List.of(new PrayerTopic(churchInfo, "기도제목", 1));

        given(churchInfoRepository.findByChurchId(ChurchId.SUWON)).willReturn(Optional.of(churchInfo));
        given(prayerTopicRepository.findByChurchInfoOrderBySortOrderAsc(churchInfo)).willReturn(prayerTopics);
        given(postImageRepository.findRandomImagePathsByChurch("SUWON")).willReturn(List.of());

        // when
        ChurchInfoResponse response = churchInfoService.getChurchInfo(ChurchId.SUWON);

        // then
        assertThat(response.getRandomPhotos()).isEmpty();
    }
}
