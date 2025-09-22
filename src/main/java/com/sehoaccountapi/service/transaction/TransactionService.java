package com.sehoaccountapi.service.transaction;

import com.sehoaccountapi.config.RestPage;
import com.sehoaccountapi.repository.book.Book;
import com.sehoaccountapi.repository.book.BookRepository;
import com.sehoaccountapi.repository.category.Category;
import com.sehoaccountapi.repository.category.CategoryRepository;
import com.sehoaccountapi.repository.transaction.Transaction;
import com.sehoaccountapi.repository.transaction.TransactionRepository;
import com.sehoaccountapi.repository.transaction.TransactionType;
import com.sehoaccountapi.service.exceptions.BadRequestException;
import com.sehoaccountapi.service.exceptions.NotFoundException;
import com.sehoaccountapi.web.dto.transactions.TransactionRequest;
import com.sehoaccountapi.web.dto.transactions.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public RestPage<TransactionResponse> getAllByUserIdAndBookId(Long userId, Long bookId, Pageable pageable) {
        Book book = bookRepository.findByUserIdAndId(userId, bookId)
                .orElseThrow(()->new NotFoundException("해당 가계부를 찾을 수 없습니다.", bookId));

        return new RestPage<>(transactionRepository.findByBookId(book.getId(), pageable)
                .map(this::convertToTransactionResponse));
    }

    @Transactional
    public TransactionResponse getTransactionById(Long userId, Long bookId, Long transactionId) {
        Book book = bookRepository.findByUserIdAndId(userId, bookId)
                .orElseThrow(()->new NotFoundException("해당 가계부를 찾을 수 없습니다.", bookId));

        return transactionRepository.findByBookIdAndId(book.getId(), transactionId)
                .map(this::convertToTransactionResponse)
                .orElseThrow(()->new NotFoundException("해당 거래내역을 찾을 수 없습니다.", transactionId));
    }

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest transactionRequest) {
        Book book = bookRepository.findByUserIdAndId(userId, transactionRequest.getBookId())
                .orElseThrow(()->new NotFoundException("해당 가계부를 찾을 수 없습니다.", transactionRequest.getBookId()));

        Category category = categoryRepository.findById(transactionRequest.getCategoryId())
                .orElseThrow(()->new NotFoundException("해당 카테고리를 찾을 수 없습니다.", transactionRequest.getCategoryId()));

        if(transactionRequest.getAmount() == null) {
            throw new BadRequestException("가격을 입력해주세요.", null);
        }

        if(transactionRequest.getType() ==null || transactionRequest.getType().isEmpty()) {
            throw new BadRequestException("거래 타입을 입력해주세요.", null);
        }

        Transaction transaction = Transaction.builder()
                .book(book)
                .category(category)
                .amount(transactionRequest.getAmount())
                .type(TransactionType.valueOf(transactionRequest.getType()))
                .note(transactionRequest.getNote())
                .transactionDate(LocalDateTime.now())
                .dedupeKey(transactionRequest.getDedupeKey())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return convertToTransactionResponse(savedTransaction);
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook().getId())
                .categoryId(transaction.getCategory().getId())
                .amount(transaction.getAmount())
                .type(transaction.getType().toString())
                .note(transaction.getNote())
                .transactionDate(transaction.getTransactionDate().toString())
                .dedupeKey(transaction.getDedupeKey())
                .build();
    }
}
