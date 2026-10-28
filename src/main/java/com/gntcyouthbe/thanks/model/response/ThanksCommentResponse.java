package com.gntcyouthbe.thanks.model.response;

import com.gntcyouthbe.thanks.domain.ThanksComment;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.AuthorInfo;
import java.time.LocalDateTime;

// isMine은 요청한 사람 기준이라, MASTER라도 남의 댓글이면 false다. 지우기 버튼을 보여 줄지는 FE가 정한다.
public record ThanksCommentResponse(
        Long id,
        AuthorInfo author,
        String content,
        boolean isMine,
        LocalDateTime createdAt
) {

    public static ThanksCommentResponse of(ThanksComment comment, String authorProfileImageUrl, Long viewerId) {
        return new ThanksCommentResponse(
                comment.getId(),
                AuthorInfo.of(comment.getUser(), authorProfileImageUrl),
                comment.getContent(),
                comment.isWrittenBy(viewerId),
                comment.getCreatedAt()
        );
    }
}
