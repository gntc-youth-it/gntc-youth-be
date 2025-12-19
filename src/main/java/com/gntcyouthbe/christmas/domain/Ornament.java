package com.gntcyouthbe.christmas.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import com.gntcyouthbe.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ornament")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ornament extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrnamentType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false, length = 50)
    private String writerName;

    @Column(nullable = false, length = 200)
    private String message;

    @Embedded
    private OrnamentPosition position;

    public Ornament(User user, String writerName, OrnamentType type, String message, double x, double y) {
        this.user = user;
        this.writerName = writerName;
        this.type = type;
        this.message = message;
        this.position = new OrnamentPosition(x, y);
    }
}
