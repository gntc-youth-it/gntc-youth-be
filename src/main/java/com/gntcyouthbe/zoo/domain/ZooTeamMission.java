package com.gntcyouthbe.zoo.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.file.domain.UploadedFile;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 처음 제출한 시각은 createdAt, 마지막으로 고친 시각은 updatedAt이다.
@Entity
@Table(name = "zoo_team_mission",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_zoo_team_mission_team_stop", columnNames = {"team_id", "stop_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZooTeamMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ZooTeam team;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_id", nullable = false, length = 20)
    private ZooStop stop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UploadedFile photo;

    @ElementCollection
    @CollectionTable(
            name = "zoo_team_mission_answer",
            joinColumns = @JoinColumn(name = "mission_id"),
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @OrderColumn(name = "sort_order")
    private List<ZooMissionAnswer> answers = new ArrayList<>();

    public ZooTeamMission(ZooTeam team, ZooStop stop, UploadedFile photo, List<ZooMissionAnswer> answers) {
        this.team = team;
        this.stop = stop;
        this.photo = photo;
        this.answers.addAll(answers);
    }

    // 답 목록만 바뀌면 Hibernate가 미션을 수정된 것으로 보지 않아 @LastModifiedDate가 돌지 않는다.
    // 그래서 다시 낼 때마다 updatedAt을 직접 바꿔 수정 시각이 갱신되게 한다.
    public void resubmit(UploadedFile photo, List<ZooMissionAnswer> answers) {
        this.photo = photo;
        this.answers.clear();
        this.answers.addAll(answers);
        this.updatedAt = LocalDateTime.now();
    }
}
