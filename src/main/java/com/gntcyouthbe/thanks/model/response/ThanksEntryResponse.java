package com.gntcyouthbe.thanks.model.response;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.thanks.domain.ThanksComment;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksMediaType;
import com.gntcyouthbe.thanks.domain.ThanksSlot;
import com.gntcyouthbe.user.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// record여야 isMine 키가 그대로 나간다. Lombok @Getter + boolean isMine은 "mine"으로 직렬화된다.
public record ThanksEntryResponse(
        Long id,
        LocalDate missionDate,
        ThanksSlot slot,
        String content,
        MediaInfo media,
        AuthorInfo author,
        List<ThanksReactionResponse> reactions,
        long commentCount,
        // 댓글이 없으면 null이다
        LatestCommentInfo latestComment,
        boolean isMine,
        LocalDateTime createdAt
) {

    // url은 file_path 그대로다(uploads/...). FE가 CDN 주소로 바꾼다.
    public record MediaInfo(
            Long fileId,
            String url,
            ThanksMediaType type
    ) {
        // 올릴 때 사진이나 영상인지 확인했으므로 종류는 늘 정해진다
        public static MediaInfo from(UploadedFile file) {
            return new MediaInfo(
                    file.getId(),
                    file.getFilePath(),
                    ThanksMediaType.from(file.getContentType()).orElseThrow()
            );
        }
    }

    // 프로필 사진이 없으면 profileImageUrl은 null이다
    public record AuthorInfo(
            Long userId,
            String name,
            String profileImageUrl
    ) {
        public static AuthorInfo of(User user, String profileImageUrl) {
            return new AuthorInfo(user.getId(), user.getName(), profileImageUrl);
        }
    }

    // 목록 카드에 말풍선으로 붙는 가장 최근 댓글
    public record LatestCommentInfo(
            AuthorInfo author,
            String content
    ) {
        public static LatestCommentInfo of(ThanksComment comment, String authorProfileImageUrl) {
            return new LatestCommentInfo(AuthorInfo.of(comment.getUser(), authorProfileImageUrl), comment.getContent());
        }
    }

    // 감사 하나의 댓글 수와 가장 최근 댓글
    public record CommentSummary(
            long count,
            LatestCommentInfo latestComment
    ) {
        public static final CommentSummary EMPTY = new CommentSummary(0, null);
    }

    public static ThanksEntryResponse of(ThanksEntry entry, String authorProfileImageUrl, Long viewerId,
            List<ThanksReactionResponse> reactions, CommentSummary comments) {
        return new ThanksEntryResponse(
                entry.getId(),
                entry.getMissionDate(),
                entry.getSlot(),
                entry.getContent(),
                MediaInfo.from(entry.getFile()),
                AuthorInfo.of(entry.getUser(), authorProfileImageUrl),
                reactions,
                comments.count(),
                comments.latestComment(),
                entry.isWrittenBy(viewerId),
                entry.getCreatedAt()
        );
    }
}
