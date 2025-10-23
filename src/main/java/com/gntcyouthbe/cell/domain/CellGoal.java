package com.gntcyouthbe.cell.domain;

import com.gntcyouthbe.bible.domain.Verse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cell_goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CellGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name = "cell_id", nullable = false)
    private Cell cell;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "start_verse_id", nullable = false)
    private Verse startVerse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "end_verse_id", nullable = false)
    private Verse endVerse;

    @Column(length=100, nullable=false)
    private String title;

    public int getStartSequence() {
        return startVerse.getSequence();
    }

    public int getEndSequence() {
        return endVerse.getSequence();
    }

    public int getTotalVerses() {
        return getEndSequence() - getStartSequence() + 1;
    }
}
