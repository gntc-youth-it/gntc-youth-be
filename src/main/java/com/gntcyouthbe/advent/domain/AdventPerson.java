package com.gntcyouthbe.advent.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "advent_persons",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_advent_person_name_temple_batch",
                columnNames = {"name", "temple", "batch"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdventPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String temple;

    @Column(nullable = false)
    private Integer batch;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<AdventVerse> verses = new ArrayList<>();

    @Builder
    public AdventPerson(String name, String temple, Integer batch) {
        this.name = name;
        this.temple = temple;
        this.batch = batch;
    }

    public void addVerse(AdventVerse verse) {
        verses.add(verse);
        verse.setPerson(this);
    }
}
