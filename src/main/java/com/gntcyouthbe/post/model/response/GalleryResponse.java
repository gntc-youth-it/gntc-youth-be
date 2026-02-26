package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.post.domain.PostImage;
import java.util.List;
import lombok.Getter;

@Getter
public class GalleryResponse {

    private final List<GalleryImageResponse> images;
    private final Long nextCursor;
    private final boolean hasNext;

    private GalleryResponse(List<GalleryImageResponse> images, Long nextCursor, boolean hasNext) {
        this.images = images;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static GalleryResponse of(List<PostImage> postImages, int size) {
        boolean hasNext = postImages.size() > size;
        List<PostImage> content = hasNext ? postImages.subList(0, size) : postImages;

        List<GalleryImageResponse> images = content.stream()
                .map(GalleryImageResponse::from)
                .toList();

        Long nextCursor = content.isEmpty() ? null : content.getLast().getId();

        return new GalleryResponse(images, hasNext ? nextCursor : null, hasNext);
    }

    @Getter
    public static class GalleryImageResponse {
        private final Long id;
        private final String url;

        private GalleryImageResponse(Long id, String url) {
            this.id = id;
            this.url = url;
        }

        public static GalleryImageResponse from(PostImage postImage) {
            return new GalleryImageResponse(
                    postImage.getId(),
                    postImage.getUploadedFile().getFilePath()
            );
        }
    }
}
