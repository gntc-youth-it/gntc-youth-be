package com.gntcyouthbe.thanks.model.response;

import com.gntcyouthbe.file.domain.UploadedFile;
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
        // 댓글은 4단계에서 채운다. FE 타입이 이 모양이라 필드는 지금부터 빈 값으로 내려 준다.
        long commentCount,
        Object latestComment,
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

    public static ThanksEntryResponse of(ThanksEntry entry, String authorProfileImageUrl, Long viewerId,
            List<ThanksReactionResponse> reactions) {
        return new ThanksEntryResponse(
                entry.getId(),
                entry.getMissionDate(),
                entry.getSlot(),
                entry.getContent(),
                MediaInfo.from(entry.getFile()),
                AuthorInfo.of(entry.getUser(), authorProfileImageUrl),
                reactions,
                0,
                null,
                entry.isWrittenBy(viewerId),
                entry.getCreatedAt()
        );
    }
}
