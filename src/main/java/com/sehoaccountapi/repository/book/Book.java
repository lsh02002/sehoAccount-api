package com.example.ledger.domain.book;

import com.example.ledger.domain.category.Category;
import com.example.ledger.domain.common.BaseTimeEntity;
import com.example.ledger.domain.transaction.Transaction;
import com.example.ledger.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_owner", columnList = "owner_id")
})
public class Book extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
}
