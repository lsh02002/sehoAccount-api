package com.sehoaccountapi.repository.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByBookId(Long bookId, Pageable pageable);
    Optional<Transaction> findByBookIdAndId(Long bookId, Long id);
    boolean existsByDedupeKey(String dedupeKey);
    boolean existsByDedupeKeyAndIdNot(String dedupeKey, Long id);
    void deleteByBookIdAndId(Long bookId, Long id);
}
