package com.gntcyouthbe.thanks.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 한 사람은 한 감사에 서로 다른 반응을 여러 개 누를 수 있고, 같은 반응은 하나만 남는다(uk_thanks_reaction_entry_user_emoji).
// 이 unique 제약의 첫 열이 entry_id라서, 감사별로 조회할 때도 따로 인덱스를 두지 않고 이 인덱스를 쓴다.
@Entity
@Table(name = "thanks_reaction",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_thanks_reaction_entry_user_emoji", columnNames = {"entry_id", "user_id", "emoji"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThanksReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ThanksEntry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji", nullable = false, length = 20)
    private ThanksEmoji emoji;

    public ThanksReaction(ThanksEntry entry, User user, ThanksEmoji emoji) {
        this.entry = entry;
        this.user = user;
        this.emoji = emoji;
    }
}
