package com.gntcyouthbe.bible.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gntcyouthbe.bible.domain.ChapterVerse;
import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.bible.domain.VerseCopy;
import com.gntcyouthbe.cell.domain.CellGoal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChapterResponse {
    private final List<VerseItem> verses;

    public ChapterResponse(final ChapterVerse verses, final CellGoal goal, final List<VerseCopy> copies) {
        final int startSequence = goal.getStartSequence();
        final int endSequence = goal.getEndSequence();
        final var copiedSet = new HashSet<>(getVerses(copies));

        final List<VerseItem> tmp = new ArrayList<>(verses.size());
        for (Verse v : verses.getVerses()) {
            final boolean isMission = v.getSequence() >= startSequence && v.getSequence() <= endSequence;
            final boolean isCopied  = copiedSet.contains(v);
            tmp.add(new VerseItem(v.getId(), v.getNumber(), v.getContent(), isMission, isCopied));
        }
        this.verses = tmp;
    }

    private List<Verse> getVerses(List<VerseCopy> copies) {
        List<Verse> verses = new ArrayList<>();
        for (VerseCopy copy : copies) {
            verses.add(copy.getVerse());
        }
        return verses;
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class VerseItem {
        private final Long verseId;

        private final int verseNumber;

        private final String content;

        @JsonProperty("is_mission")
        private final boolean mission;

        @JsonProperty("is_copied")
        private final boolean copied;

        public VerseItem(Long verseId, int verseNumber, String content, boolean mission, boolean copied) {
            this.verseId = verseId;
            this.verseNumber = verseNumber;
            this.content = content;
            this.mission = mission;
            this.copied = copied;
        }
    }
}
