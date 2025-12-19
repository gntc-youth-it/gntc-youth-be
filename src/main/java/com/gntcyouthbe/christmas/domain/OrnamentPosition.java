package com.gntcyouthbe.christmas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrnamentPosition {

    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    private double y;

    public OrnamentPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
