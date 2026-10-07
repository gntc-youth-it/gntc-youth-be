package com.gntcyouthbe.zoo.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zoo_team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZooTeam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ZooCourse course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ZooTeamStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User leader;

    private LocalDateTime startedAt;

    public ZooTeam(String name, ZooCourse course, User leader) {
        this.name = name;
        this.course = course;
        this.leader = leader;
        this.status = ZooTeamStatus.RECRUITING;
    }

    public boolean isStarted() {
        return status == ZooTeamStatus.STARTED;
    }

    public boolean isLeader(Long userId) {
        return leader.getId().equals(userId);
    }

    public void changeCourse(ZooCourse course) {
        this.course = course;
    }

    // 두 번 눌러도 처음 출발한 시각을 유지한다
    public void start() {
        if (isStarted()) {
            return;
        }
        this.status = ZooTeamStatus.STARTED;
        this.startedAt = LocalDateTime.now();
    }

    public void changeLeader(User leader) {
        this.leader = leader;
    }
}
