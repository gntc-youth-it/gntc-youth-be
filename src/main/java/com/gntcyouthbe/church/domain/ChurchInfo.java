package com.gntcyouthbe.church.domain;

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

    public ChurchInfo(ChurchId churchId) {
        this.churchId = churchId;
    }

    public void updateGroupPhoto(UploadedFile groupPhoto) {
        this.groupPhoto = groupPhoto;
    }
}
