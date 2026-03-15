package com.gntcyouthbe.video.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.video.domain.Video;
import com.gntcyouthbe.video.model.request.VideoCreateRequest;
import com.gntcyouthbe.video.model.response.VideoResponse;
import com.gntcyouthbe.video.repository.VideoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    @Test
    @DisplayName("영상을 등록하면 제목, 링크, 행사 정보가 저장된다")
    void createVideo_success() {
        // given
        VideoCreateRequest request = createRequest("수련회 찬양", "https://youtube.com/watch?v=abc", PostSubCategory.RETREAT_2026_WINTER);

        given(videoRepository.save(any(Video.class))).willAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            ReflectionTestUtils.setField(video, "id", 1L);
            return video;
        });

        // when
        VideoResponse response = videoService.createVideo(request);

        // then
        assertThat(response.getTitle()).isEqualTo("수련회 찬양");
        assertThat(response.getLink()).isEqualTo("https://youtube.com/watch?v=abc");
        assertThat(response.getSubCategory()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER);
    }

    @Test
    @DisplayName("행사별 영상을 조회하면 해당 행사의 영상만 반환된다")
    void getVideos_withSubCategory() {
        // given
        Video video = createVideo(1L, "수련회 영상", "https://youtube.com/watch?v=1", PostSubCategory.RETREAT_2026_WINTER);

        given(videoRepository.findBySubCategory(PostSubCategory.RETREAT_2026_WINTER))
                .willReturn(List.of(video));

        // when
        List<VideoResponse> responses = videoService.getVideos(PostSubCategory.RETREAT_2026_WINTER);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSubCategory()).isEqualTo(PostSubCategory.RETREAT_2026_WINTER);
    }

    @Test
    @DisplayName("행사 지정 없이 조회하면 전체 영상이 반환된다")
    void getVideos_withoutSubCategory() {
        // given
        Video video1 = createVideo(1L, "수련회 영상", "https://youtube.com/watch?v=1", PostSubCategory.RETREAT_2026_WINTER);
        Video video2 = createVideo(2L, "기타 영상", "https://youtube.com/watch?v=2", PostSubCategory.NONE);

        given(videoRepository.findAll()).willReturn(List.of(video1, video2));

        // when
        List<VideoResponse> responses = videoService.getVideos(null);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("영상을 삭제하면 정상적으로 삭제된다")
    void deleteVideo_success() {
        // given
        Video video = createVideo(1L, "삭제할 영상", "https://youtube.com/watch?v=del", PostSubCategory.RETREAT_2026_WINTER);

        given(videoRepository.findById(1L)).willReturn(Optional.of(video));

        // when
        videoService.deleteVideo(1L);

        // then
        then(videoRepository).should().delete(video);
    }

    @Test
    @DisplayName("존재하지 않는 영상을 삭제하면 예외가 발생한다")
    void deleteVideo_notFound_throwsException() {
        // given
        given(videoRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> videoService.deleteVideo(999L))
                .isInstanceOf(EntityNotFoundException.class);

        then(videoRepository).should(never()).delete(any());
    }

    private Video createVideo(Long id, String title, String link, PostSubCategory subCategory) {
        Video video = new Video(title, link, subCategory);
        ReflectionTestUtils.setField(video, "id", id);
        return video;
    }

    private VideoCreateRequest createRequest(String title, String link, PostSubCategory subCategory) {
        VideoCreateRequest request = new VideoCreateRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "link", link);
        ReflectionTestUtils.setField(request, "subCategory", subCategory);
        return request;
    }
}
