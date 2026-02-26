package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostCategory;
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
    private final List<ChurchId> churchIds;
    private final LocalDateTime createdAt;

    private PostResponse(Long id, Long authorId, String authorName, PostSubCategory subCategory,
            PostCategory category, PostStatus status, String content,
            List<String> hashtags, List<ChurchId> churchIds, LocalDateTime createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.authorName = authorName;
        this.subCategory = subCategory;
        this.category = category;
        this.status = status;
        this.content = content;
        this.hashtags = hashtags;
        this.churchIds = churchIds;
        this.createdAt = createdAt;
    }

    public static PostResponse from(Post post) {
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
                post.getCreatedAt()
        );
    }
}
