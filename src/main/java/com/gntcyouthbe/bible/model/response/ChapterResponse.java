package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.ChapterVerse;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.cell.domain.CellGoal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChapterResponse {
    private final List<VerseItem> verses;

    public ChapterResponse(final ChapterVerse verses, final CellGoal goal, final List<Long> copiedIds) {
        final int startSequence = goal.getStartSequence();
        final int endSequence = goal.getEndSequence();;
        final var copiedSet = new java.util.HashSet<>(copiedIds);

        final List<VerseItem> tmp = new ArrayList<>(verses.size());
        for (Verse v : verses.getVerses()) {
            final boolean isMission = v.getSequence() >= startSequence && v.getSequence() <= endSequence;
            final boolean isCopied  = copiedSet.contains(v.getId());
            tmp.add(new VerseItem(v.getId(), v.getNumber(), isMission, isCopied));
        }
        this.verses = tmp;
    }

    @Getter
    public static class VerseItem {
        private final Long verseId;

        private final int verseNumber;

        @JsonProperty("is_mission")
        private final boolean mission;

        @JsonProperty("is_copied")
        private final boolean copied;

        public VerseItem(Long verseId, int verseNumber, boolean mission, boolean copied) {
            this.verseId = verseId;
            this.verseNumber = verseNumber;
            this.mission = mission;
            this.copied = copied;
        }
    }
}
