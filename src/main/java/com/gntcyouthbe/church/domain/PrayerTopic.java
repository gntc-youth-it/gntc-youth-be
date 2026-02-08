package com.gntcyouthbe.church.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prayer_topic")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrayerTopic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_info_id", nullable = false,
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ChurchInfo churchInfo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer sortOrder;

    public PrayerTopic(ChurchInfo churchInfo, String content, Integer sortOrder) {
        this.churchInfo = churchInfo;
        this.content = content;
        this.sortOrder = sortOrder;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
