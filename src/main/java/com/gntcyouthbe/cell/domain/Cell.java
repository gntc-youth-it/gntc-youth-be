package com.gntcyouthbe.cell.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cell")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cell {

    @Id
    private Long id;

    @Column(length = 10)
    private String name;

    @OneToMany(mappedBy = "cell", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CellMember> members = new HashSet<>();
}
