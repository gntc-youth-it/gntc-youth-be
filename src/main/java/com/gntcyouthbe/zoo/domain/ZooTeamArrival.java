package com.gntcyouthbe.zoo.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 도착 시각은 createdAt, 기록한 사람은 createdBy이다.
@Entity
@Table(name = "zoo_team_arrival",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_zoo_team_arrival_team_stop", columnNames = {"team_id", "stop_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZooTeamArrival extends BaseEntity {

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

    public ZooTeamArrival(ZooTeam team, ZooStop stop) {
        this.team = team;
        this.stop = stop;
    }
}
