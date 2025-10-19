package com.gntcyouthbe.cell.domain;

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

@Entity
@Table(name="cell_member",
        uniqueConstraints=@UniqueConstraint(name="uk_cell_user", columnNames={"cell_id","user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CellMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY, optional=false)
    @JoinColumn(name="cell_id", nullable=false,
            foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Cell cell;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id", nullable=false,
            foreignKey=@ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length=20)
    private CellRole role;
}
