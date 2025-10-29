package com.gntcyouthbe.bible.domain;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChapterVerse {

    private final List<Verse> verses;

    public ChapterVerse(final List<Verse> verses) {
        validVersesAtSameChapter(verses);
        this.verses = verses;
        sortVerses();
    }

    private void validVersesAtSameChapter(final List<Verse> verses) {
        final int chapter = verses.getFirst().getChapter();
        for (Verse verse : verses) {
            if (verse.getChapter() != chapter) {
                throw new IllegalArgumentException("하나의 장에 속하지 않은 구절이 포함되어 있습니다.");
            }
        }
    }

    private void sortVerses() {
        verses.sort(Comparator.comparingInt(Verse::getSequence));
    }

    public List<Verse> getVerses() {
        return Collections.unmodifiableList(verses);
    }

    public int size() {
        return verses.size();
    }

    public List<Long> getVerseIds() {
        return verses.stream()
                .map(Verse::getId)
                .toList();
    }

}
