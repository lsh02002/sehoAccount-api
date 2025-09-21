package com.example.ledger.domain.transaction;

import com.example.ledger.domain.book.Book;
import com.example.ledger.domain.category.Category;
import com.example.ledger.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_book_date", columnList = "book_id, transactionDate"),
        @Index(name = "idx_tx_category", columnList = "category_id")
})
public class Transaction extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionType type; // INCOME / EXPENSE

    @Column(columnDefinition = "text")
    private String note;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 128, unique = true)
    private String dedupeKey;
}
