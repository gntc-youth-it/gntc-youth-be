package com.gntcyouthbe.church.model.response;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.domain.ChurchInfo;
import com.gntcyouthbe.church.domain.PrayerTopic;
import com.gntcyouthbe.file.domain.UploadedFile;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChurchInfoResponse {

    private final ChurchId churchId;
    private final String groupPhotoPath;
    private final String instagramId;
    private final ThemeVerseResponse themeVerse;
    private final List<PrayerTopicResponse> prayerTopics;
    private final List<String> randomPhotos;

    public static ChurchInfoResponse from(ChurchInfo churchInfo, List<PrayerTopic> prayerTopics,
            List<String> randomPhotos) {
        return new ChurchInfoResponse(
                churchInfo.getChurchId(),
                Optional.ofNullable(churchInfo.getGroupPhoto()).map(UploadedFile::getFilePath).orElse(null),
                churchInfo.getInstagramId(),
                Optional.ofNullable(churchInfo.getThemeVerse()).map(ThemeVerseResponse::from).orElse(null),
                prayerTopics.stream()
                        .map(PrayerTopicResponse::from)
                        .toList(),
                randomPhotos
        );
    }

    @Getter
    @AllArgsConstructor
    public static class ThemeVerseResponse {

        private final Long verseId;
        private final String bookName;
        private final int chapter;
        private final int verseNumber;
        private final String content;

        public static ThemeVerseResponse from(Verse verse) {
            return new ThemeVerseResponse(
                    verse.getId(),
                    verse.getBook().getName(),
                    verse.getChapter(),
                    verse.getNumber(),
                    verse.getContent()
            );
        }
    }
}
