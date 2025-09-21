package com.example.ledger.domain.category;

import com.example.ledger.domain.book.Book;
import com.example.ledger.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_book_name", columnNames = {"book_id", "name"})
}, indexes = {
        @Index(name = "idx_categories_book", columnList = "book_id")
})
public class Category extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Allow null for global categories if desired
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CategoryType type; // INCOME / EXPENSE
}
