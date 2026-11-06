package com.gntcyouthbe.thanks.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 감사로그의 구역이다. 관리 화면이 없어서 목록과 순서는 운영 DB에 직접 넣는다.
// 2025년 성경 필사의 cell 테이블이 운영 DB에 남아 있을 수 있어서, 섞이지 않게 따로 둔다.
@Entity
@Table(name = "thanks_cell")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThanksCell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    // 화면에 보여 줄 순서
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ThanksCell(String name, int sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }
}
