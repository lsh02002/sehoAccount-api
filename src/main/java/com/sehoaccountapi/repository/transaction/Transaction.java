package com.sehoaccountapi.repository.transaction;

import com.sehoaccountapi.repository.book.Book;
import com.sehoaccountapi.repository.category.Category;
import com.sehoaccountapi.repository.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
