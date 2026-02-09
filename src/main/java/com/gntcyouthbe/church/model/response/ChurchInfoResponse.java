package com.gntcyouthbe.church.model.response;

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
    private final List<PrayerTopicResponse> prayerTopics;

    public static ChurchInfoResponse from(ChurchInfo churchInfo, List<PrayerTopic> prayerTopics) {
        return new ChurchInfoResponse(
                churchInfo.getChurchId(),
                Optional.ofNullable(churchInfo.getGroupPhoto()).map(UploadedFile::getFilePath).orElse(null),
                prayerTopics.stream()
                        .map(PrayerTopicResponse::from)
                        .toList()
        );
    }
}
