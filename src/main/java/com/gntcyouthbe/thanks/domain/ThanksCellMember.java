package com.gntcyouthbe.thanks.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.user.domain.User;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사람과 구역을 잇는다. 프로필 행이 없는 사람도 고를 수 있어야 해서 user_profile에 두지 않는다.
// 한 사람은 구역 하나만 갖는다(uk_thanks_cell_member_user). 감사 기록에는 구역을 저장하지 않으므로,
// 나중에 구역별로 취합할 때는 thanks_entry.user_id와 이어서 사람의 지금 구역으로 묶는다.
@Entity
@Table(name = "thanks_cell_member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_thanks_cell_member_user", columnNames = {"user_id"})
        },
        indexes = {
                @Index(name = "idx_thanks_cell_member_cell_id", columnList = "cell_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThanksCellMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ThanksCell cell;

    public ThanksCellMember(User user, ThanksCell cell) {
        this.user = user;
        this.cell = cell;
    }

    public void changeCell(ThanksCell cell) {
        this.cell = cell;
    }
}
