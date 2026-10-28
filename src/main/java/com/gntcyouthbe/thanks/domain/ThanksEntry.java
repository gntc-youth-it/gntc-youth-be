package com.gntcyouthbe.thanks.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.user.domain.User;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 한 사람은 날짜·칸마다 하나씩 올린다(uk_thanks_entry_user_date_slot).
// 감사를 지울 때 파일도 함께 지우므로, 파일 하나는 감사 하나에만 붙는다(uk_thanks_entry_file_id).
@Entity
@Table(name = "thanks_entry",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_thanks_entry_user_date_slot", columnNames = {"user_id", "mission_date", "slot"}),
                @UniqueConstraint(name = "uk_thanks_entry_file_id", columnNames = {"file_id"})
        },
        indexes = {
                @Index(name = "idx_thanks_entry_mission_date", columnList = "mission_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThanksEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    // 올린 시각의 한국 날짜
    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot", nullable = false, length = 20)
    private ThanksSlot slot;

    // 80자까지만 받고, 컬럼은 여유를 둔다
    @Column(nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UploadedFile file;

    public ThanksEntry(User user, LocalDate missionDate, ThanksSlot slot, String content, UploadedFile file) {
        this.user = user;
        this.missionDate = missionDate;
        this.slot = slot;
        this.content = content;
        this.file = file;
    }

    public boolean isWrittenBy(Long userId) {
        return user.getId().equals(userId);
    }

    // 고칠 수 있는 것은 감사제목뿐이다. 사진·날짜·칸은 그대로 둔다.
    public void updateContent(String content) {
        this.content = content;
    }
}
