package com.gntcyouthbe.church.repository;

import com.gntcyouthbe.church.domain.ChurchInfo;
import com.gntcyouthbe.church.domain.PrayerTopic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrayerTopicRepository extends JpaRepository<PrayerTopic, Long> {

    List<PrayerTopic> findByChurchInfoOrderBySortOrderAsc(ChurchInfo churchInfo);

    void deleteByChurchInfo(ChurchInfo churchInfo);
}
