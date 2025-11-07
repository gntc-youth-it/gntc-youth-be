package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RankResponse {

    private final List<RankItem> ranks;

    public RankResponse(final List<VerseCopyRepository.TopRow> topRows) {
        this.ranks = topRows.stream()
                .map(RankItem::new)
                .toList();
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RankItem {
        private final String userName;
        private final long copyCount;

        public RankItem(final VerseCopyRepository.TopRow row) {
            this.userName = row.getUserName();
            this.copyCount = row.getCopyCount();
        }
    }
}
