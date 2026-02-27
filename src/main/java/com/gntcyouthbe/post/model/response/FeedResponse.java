package com.gntcyouthbe.post.model.response;

import com.gntcyouthbe.post.domain.Post;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class FeedResponse {

    private final List<PostResponse> posts;
    private final Long nextCursor;
    private final boolean hasNext;

    private FeedResponse(List<PostResponse> posts, Long nextCursor, boolean hasNext) {
        this.posts = posts;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static FeedResponse of(List<Post> posts, int size, Map<Long, Long> commentCounts,
            Map<Long, String> profileImageUrls) {
        boolean hasNext = posts.size() > size;
        List<Post> content = hasNext ? posts.subList(0, size) : posts;

        List<PostResponse> postResponses = content.stream()
                .map(post -> PostResponse.from(
                        post,
                        commentCounts.getOrDefault(post.getId(), 0L),
                        profileImageUrls.get(post.getAuthor().getId())))
                .toList();

        Long nextCursor = content.isEmpty() ? null : content.getLast().getId();

        return new FeedResponse(postResponses, hasNext ? nextCursor : null, hasNext);
    }
}
