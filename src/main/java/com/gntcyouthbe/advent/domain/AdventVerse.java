package com.gntcyouthbe.advent.domain;

import com.gntcyouthbe.bible.domain.BookName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "advent_verses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdventVerse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private AdventPerson person;

    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_name", nullable = false, length = 30)
    private BookName bookName;

    @Column(nullable = false)
    private Integer chapter;

    @Column(nullable = false)
    private Integer verse;

    @Builder
    public AdventVerse(Integer sequence, BookName bookName, Integer chapter, Integer verse) {
        this.sequence = sequence;
        this.bookName = bookName;
        this.chapter = chapter;
        this.verse = verse;
    }

    void setPerson(AdventPerson person) {
        this.person = person;
    }

    public String getBookDisplayName() {
        return bookName.getDisplayName();
    }

    public String getFullReference() {
        return bookName.getDisplayName() + " " + chapter + ":" + verse;
    }
}
