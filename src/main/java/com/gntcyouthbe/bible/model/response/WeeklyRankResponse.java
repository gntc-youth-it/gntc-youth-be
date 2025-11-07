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
public class WeeklyRankResponse {

    private final LocalDate startDate;
    private final LocalDate endDate;

    private final List<WeeklyRankItem> ranks;

    public WeeklyRankResponse(final ZonedDateTime startTime, final ZonedDateTime endTime, final List<VerseCopyRepository.TopRow> data) {
        this.startDate = startTime.toLocalDate();
        this.endDate = endTime.toLocalDate().minusDays(1);
        this.ranks = data.stream()
                .map(WeeklyRankItem::new)
                .toList();
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WeeklyRankItem {
        private final String userName;

        private final long copyCount;

        public WeeklyRankItem(final VerseCopyRepository.TopRow row) {
            this.userName = row.getUserName();
            this.copyCount = row.getCopyCount();
        }
    }
}
