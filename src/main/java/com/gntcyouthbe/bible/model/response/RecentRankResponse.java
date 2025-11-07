package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.VerseCopy;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RecentRankResponse {

    private final List<RecentRankItem> ranks;

    public RecentRankResponse(final List<VerseCopy> copies) {
        this.ranks = copies.stream()
                .map(RecentRankItem::new)
                .toList();
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RecentRankItem {
        private final String userName;

        private final String verseName;

        private final LocalDateTime copiedAt;

        public RecentRankItem(final VerseCopy verseCopy) {
            this.userName = verseCopy.getUserName();
            this.verseName = verseCopy.getVerseName();
            this.copiedAt = verseCopy.getCreatedAt();
        }
    }
}
