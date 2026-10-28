package com.gntcyouthbe.thanks.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.user.domain.User;
import jakarta.persistence.Column;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 감사로그 전용 댓글이다(게시판의 post_comment와 따로 둔다). 수정과 답글은 없다.
// id가 작은 순서가 단 순서라서, 오래된 순 정렬과 가장 최근 댓글을 id로 정한다.
@Entity
@Table(name = "thanks_comment",
        indexes = {
                @Index(name = "idx_thanks_comment_entry_id", columnList = "entry_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThanksComment extends BaseEntity {

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

    // 100자까지만 받고, 컬럼은 여유를 둔다
    @Column(nullable = false, length = 200)
    private String content;

    public ThanksComment(ThanksEntry entry, User user, String content) {
        this.entry = entry;
        this.user = user;
        this.content = content;
    }

    public boolean isWrittenBy(Long userId) {
        return user.getId().equals(userId);
    }
}
