package com.gntcyouthbe.bible.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canon_order", nullable = false)
    private Integer order;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_name", nullable = false, unique = true, length = 30)
    private BookName bookName;

    @Column(length = 50, nullable = false)
    private String name;
}
