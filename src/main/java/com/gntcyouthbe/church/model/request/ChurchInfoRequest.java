package com.gntcyouthbe.church.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChurchInfoRequest {

    private Long groupPhotoFileId;

    private String instagramId;

    @NotNull
    @Valid
    private List<PrayerTopicRequest> prayerTopics;
}
