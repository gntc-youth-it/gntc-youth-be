package com.gntcyouthbe.video.model.response;

import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.video.domain.Video;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoResponse {

    private Long id;
    private String title;
    private String link;
    private PostSubCategory subCategory;
    private LocalDateTime createdAt;

    public static VideoResponse from(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .link(video.getLink())
                .subCategory(video.getSubCategory())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
