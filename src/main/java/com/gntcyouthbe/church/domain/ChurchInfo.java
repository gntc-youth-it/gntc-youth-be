package com.gntcyouthbe.church.domain;

import com.gntcyouthbe.bible.domain.Verse;
import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.file.domain.UploadedFile;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "church_info",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_church_info_church_id", columnNames = {"church_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChurchInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "church_id", nullable = false, length = 30)
    private ChurchId churchId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_photo_id",
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UploadedFile groupPhoto;

    @Column(name = "instagram_id", length = 30)
    private String instagramId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_verse_id",
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Verse themeVerse;

    public ChurchInfo(ChurchId churchId) {
        this.churchId = churchId;
    }

    public void updateGroupPhoto(UploadedFile groupPhoto) {
        this.groupPhoto = groupPhoto;
    }

    public void updateInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }

    public void updateThemeVerse(Verse themeVerse) {
        this.themeVerse = themeVerse;
    }
}
