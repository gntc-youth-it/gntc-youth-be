package com.gntcyouthbe.church.model.response;

import com.gntcyouthbe.church.domain.PrayerTopic;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrayerTopicResponse {

    private final Long id;
    private final String content;
    private final Integer sortOrder;

    public static PrayerTopicResponse from(PrayerTopic prayerTopic) {
        return new PrayerTopicResponse(
                prayerTopic.getId(),
                prayerTopic.getContent(),
                prayerTopic.getSortOrder()
        );
    }
}
