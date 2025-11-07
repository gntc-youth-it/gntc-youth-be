package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.repository.VerseCopyRepository;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyRankResponse {

    private final LocalDate date;

    private final List<DailyRankItem> ranks;

    public DailyRankResponse(final ZonedDateTime now, final List<VerseCopyRepository.TopRow> data) {
        this.date = now.toLocalDate();
        this.ranks = data.stream()
                .map(DailyRankItem::new)
                .toList();
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DailyRankItem {
        private final String userName;

        private final long copyCount;

        public DailyRankItem(final VerseCopyRepository.TopRow row) {
            this.userName = row.getUserName();
            this.copyCount = row.getCopyCount();
        }
    }
}
