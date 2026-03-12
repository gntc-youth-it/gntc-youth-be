package com.gntcyouthbe.church.model.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChurchInfoRequest {

    private Long groupPhotoFileId;

    @Size(max = 30)
    private String instagramId;

    private Long themeVerseId;

    private boolean themeVerseIdPresent;

    @NotNull
    @Valid
    private List<PrayerTopicRequest> prayerTopics;

    public ChurchInfoRequest(Long groupPhotoFileId, String instagramId, Long themeVerseId, List<PrayerTopicRequest> prayerTopics) {
        this.groupPhotoFileId = groupPhotoFileId;
        this.instagramId = instagramId;
        this.themeVerseId = themeVerseId;
        this.themeVerseIdPresent = true;
        this.prayerTopics = prayerTopics;
    }

    @JsonSetter("themeVerseId")
    public void setThemeVerseId(Long themeVerseId) {
        this.themeVerseId = themeVerseId;
        this.themeVerseIdPresent = true;
    }
}
