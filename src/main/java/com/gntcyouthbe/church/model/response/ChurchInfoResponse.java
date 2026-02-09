package com.gntcyouthbe.church.model.response;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.church.domain.ChurchInfo;
import com.gntcyouthbe.church.domain.PrayerTopic;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChurchInfoResponse {

    private final ChurchId churchId;
    private final String groupPhotoPath;
    private final List<PrayerTopicResponse> prayerTopics;

    public static ChurchInfoResponse from(ChurchInfo churchInfo, List<PrayerTopic> prayerTopics) {
        return new ChurchInfoResponse(
                churchInfo.getChurchId(),
                churchInfo.getGroupPhoto() != null ? churchInfo.getGroupPhoto().getFilePath() : null,
                prayerTopics.stream()
                        .map(PrayerTopicResponse::from)
                        .toList()
        );
    }
}
