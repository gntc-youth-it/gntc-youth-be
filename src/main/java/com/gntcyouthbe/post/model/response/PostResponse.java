package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostCategory;
import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class PostResponse {

    private final Long id;
    private final Long authorId;
    private final String authorName;
    private final PostSubCategory subCategory;
    private final PostCategory category;
    private final PostStatus status;
    private final String content;
    private final List<String> hashtags;
    private final List<ChurchId> churches;
    private final List<ImageResponse> images;
    private final long commentCount;
    private final LocalDateTime createdAt;

    private PostResponse(Long id, Long authorId, String authorName, PostSubCategory subCategory,
            PostCategory category, PostStatus status, String content,
            List<String> hashtags, List<ChurchId> churches, List<ImageResponse> images,
            long commentCount, LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorName = authorName;
        this.subCategory = subCategory;
        this.category = category;
        this.status = status;
        this.content = content;
        this.hashtags = hashtags;
        this.churches = churches;
        this.images = images;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

    public static PostResponse from(Post post) {
        return from(post, 0);
    }

    public static PostResponse from(Post post, long commentCount) {
        List<ImageResponse> imageResponses = post.getImages().stream()
                .map(ImageResponse::from)
                .toList();

        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getName(),
                post.getSubCategory(),
                post.getCategory(),
                post.getStatus(),
                post.getContent(),
                post.getHashtags(),
                post.getChurches(),
                imageResponses,
                commentCount,
                post.getCreatedAt()
        );
    }

    @Getter
    public static class ImageResponse {
        private final Long fileId;
        private final String filePath;
        private final Integer sortOrder;

        private ImageResponse(Long fileId, String filePath, Integer sortOrder) {
            this.fileId = fileId;
            this.filePath = filePath;
            this.sortOrder = sortOrder;
        }

        public static ImageResponse from(PostImage postImage) {
            return new ImageResponse(
                    postImage.getUploadedFile().getId(),
                    postImage.getUploadedFile().getFilePath(),
                    postImage.getSortOrder()
            );
        }
    }
}
