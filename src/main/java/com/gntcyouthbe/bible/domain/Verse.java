package com.gntcyouthbe.bible.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer chapter;

    @Column(name = "verse", nullable = false)
    private Integer number;

    @Column(nullable = false, unique = true)
    private Integer sequence;

    public BookName getBookName() {
        return book.getBookName();
    }

    public int getBookOrder() {
        return book.getOrder();
    }
}
