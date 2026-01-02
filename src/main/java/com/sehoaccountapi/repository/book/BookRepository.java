package com.sehoaccountapi.repository.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByUserId(Long userId);
    Optional<Book> findByUserIdAndId(Long userId, Long bookId);
    void deleteByUserIdAndId(Long userId, Long bookId);
}
