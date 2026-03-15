package com.gntcyouthbe.video.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.video.domain.Video;
import com.gntcyouthbe.video.model.request.VideoCreateRequest;
import com.gntcyouthbe.video.model.response.VideoResponse;
import com.gntcyouthbe.video.repository.VideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.VIDEO_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    @Transactional
    public VideoResponse createVideo(VideoCreateRequest request) {
        Video video = new Video(request.getTitle(), request.getLink(), request.getSubCategory());
        videoRepository.save(video);
        return VideoResponse.from(video);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> getVideos(PostSubCategory subCategory) {
        List<Video> videos;
        if (subCategory != null) {
            videos = videoRepository.findBySubCategoryOrderByIdDesc(subCategory);
        } else {
            videos = videoRepository.findAllByOrderByIdDesc();
        }
        return videos.stream().map(VideoResponse::from).toList();
    }

    @Transactional
    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException(VIDEO_NOT_FOUND));
        videoRepository.delete(video);
    }
}
