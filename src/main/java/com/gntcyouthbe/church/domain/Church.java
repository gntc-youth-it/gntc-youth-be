package com.gntcyouthbe.church.domain;

import com.gntcyouthbe.common.orm.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "church")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Church extends BaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ChurchId id;
}
