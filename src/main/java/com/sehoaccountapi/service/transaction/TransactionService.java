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
import com.sehoaccountapi.service.exceptions.NotAcceptableException;
import com.sehoaccountapi.service.exceptions.NotFoundException;
import com.sehoaccountapi.web.dto.transactions.TransactionRequest;
import com.sehoaccountapi.web.dto.transactions.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private final ApplicationContext applicationContext;

    private final DateTimeFormatter koreanFormatter =
            DateTimeFormatter.ofPattern("yyyy. M. d. a h:mm:ss")
                    .withLocale(Locale.KOREAN);

    @Transactional
    public RestPage<TransactionResponse> getAllByUserIdAndBookId(Long userId, Long bookId, Pageable pageable) {
        TransactionService self = applicationContext.getBean(TransactionService.class);

        Book book = bookRepository.findByUserIdAndId(userId, bookId)
                .orElseThrow(() -> new NotFoundException("해당 가계부를 찾을 수 없습니다.", bookId));

        return new RestPage<>(transactionRepository.findByBookId(book.getId(), pageable)
                .map(self::getTransaction));
    }

    @Transactional
    @Cacheable(value = "transactions", key = "#transactionId")
    public TransactionResponse getTransactionById(Long userId, Long bookId, Long transactionId) {
        Book book = bookRepository.findByUserIdAndId(userId, bookId)
                .orElseThrow(() -> new NotFoundException("해당 가계부를 찾을 수 없습니다.", bookId));

        return transactionRepository.findByBookIdAndId(book.getId(), transactionId)
                .map(this::convertToTransactionResponse)
                .orElseThrow(() -> new NotFoundException("해당 거래내역을 찾을 수 없습니다.", transactionId));
    }

    @Cacheable(value = "transactions", key = "#transaction.id")
    public TransactionResponse getTransaction(Transaction transaction) {
        return convertToTransactionResponse(transaction);
    }

    @Transactional
    @CachePut(value = "transactions", key = "#result.id")
    public TransactionResponse createTransaction(Long userId, TransactionRequest transactionRequest) {
        Book book = bookRepository.findByUserIdAndId(userId, transactionRequest.getBookId())
                .orElseThrow(() -> new NotFoundException("해당 가계부를 찾을 수 없습니다.", transactionRequest.getBookId()));

        Category category = categoryRepository.findById(transactionRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("해당 카테고리를 찾을 수 없습니다.", transactionRequest.getCategoryId()));

        if (!validationDate(transactionRequest.getTransactionDate())) {
            throw new BadRequestException("입력하신 날짜가 형식에 안 맞습니다.", null);
        }

        if (transactionRequest.getAmount() == null || transactionRequest.getAmount().intValue() <= 0) {
            throw new BadRequestException("가격을 정확히 입력해주세요.", null);
        }

        if (transactionRequest.getType() == null || transactionRequest.getType().isEmpty()) {
            throw new BadRequestException("거래 타입을 입력해주세요.", null);
        }

        if (!transactionRequest.getType().equals("INCOME") && !transactionRequest.getType().equals("EXPENSE")) {
            throw new BadRequestException("거래 타입은 'INCOME' 이나 'EXPENSE' 이어야 합니다.", null);
        }

        String dedupeKey = book.getId() + "_" + transactionRequest.getAmount() + "_" + transactionRequest.getTransactionDate();

        if (transactionRepository.existsByDedupeKey(dedupeKey)) {
            throw new NotAcceptableException("입력이 이전 정보와 중복됩니다. : " + dedupeKey, dedupeKey);
        }

        LocalDateTime ldt = LocalDateTime.parse(transactionRequest.getTransactionDate(), koreanFormatter);

        Transaction transaction = Transaction.builder()
                .book(book)
                .category(category)
                .amount(transactionRequest.getAmount())
                .type(TransactionType.valueOf(transactionRequest.getType()))
                .note(transactionRequest.getNote())
                .transactionDate(ldt)
                .dedupeKey(dedupeKey)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return convertToTransactionResponse(savedTransaction);
    }

    @Transactional
    @CachePut(value = "transactions", key = "#transactionId")
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest transactionRequest) {
        Book book = bookRepository.findByUserIdAndId(userId, transactionRequest.getBookId())
                .orElseThrow(() -> new NotFoundException("해당 가계부를 찾을 수 없습니다.", transactionRequest.getBookId()));

        Category category = categoryRepository.findById(transactionRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("해당 카테고리를 찾을 수 없습니다.", transactionRequest.getCategoryId()));

        Transaction transaction = transactionRepository.findByBookIdAndId(book.getId(), transactionId)
                .orElseThrow(() -> new NotFoundException("해당 거래내역을 찾을 수 없습니다.", transactionId));

        if (!validationDate(transactionRequest.getTransactionDate())) {
            throw new BadRequestException("입력하신 날짜가 형식에 안 맞습니다.", null);
        }

        if (transactionRequest.getAmount() == null || transactionRequest.getAmount().intValue() <= 0) {
            throw new BadRequestException("가격을 정확히 입력해주세요.", null);
        }

        if (transactionRequest.getType() == null || transactionRequest.getType().isEmpty()) {
            throw new BadRequestException("거래 타입을 입력해주세요.", null);
        }

        if (!transactionRequest.getType().equals("INCOME") && !transactionRequest.getType().equals("EXPENSE")) {
            throw new BadRequestException("거래 타입은 'INCOME' 이나 'EXPENSE' 이어야 합니다.", null);
        }

        String dedupeKey = book.getId() + "_" + transactionRequest.getAmount() + "_" + transactionRequest.getTransactionDate();

        if (transactionRepository.existsByDedupeKeyAndIdNot(dedupeKey, transactionId)) {
            throw new NotAcceptableException("입력이 이전 정보와 중복됩니다. : " + dedupeKey, dedupeKey);
        }

        TransactionType type = TransactionType.valueOf(transactionRequest.getType());

// Convert to UTC view if needed
        LocalDateTime ldt = LocalDateTime.parse(transactionRequest.getTransactionDate(), koreanFormatter);

        if (!Objects.equals(transaction.getBook(), book)) transaction.setBook(book);
        if (!Objects.equals(transaction.getCategory(), category)) { transaction.setCategory(category); }
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(transactionRequest.getAmount()) != 0)
            transaction.setAmount(transactionRequest.getAmount());
        if (transaction.getType() != type) transaction.setType(type);
        if (!Objects.equals(transaction.getNote(), transactionRequest.getNote()))
            transaction.setNote(transactionRequest.getNote());
        if (!Objects.equals(transaction.getTransactionDate(), ldt)) transaction.setTransactionDate(ldt);
        if (!Objects.equals(transaction.getDedupeKey(), dedupeKey)) transaction.setDedupeKey(dedupeKey);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return convertToTransactionResponse(savedTransaction);
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBook().getId())
                .categoryName(transaction.getCategory().getName())
                .amount(transaction.getAmount())
                .type(transaction.getType().toString())
                .note(transaction.getNote())
                .transactionDate(transaction.getTransactionDate().toString())
                .dedupeKey(transaction.getDedupeKey())
                .build();
    }

    private boolean validationDate(String checkDate) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(checkDate, koreanFormatter);
            return true; // 정상적으로 Instant 생성됨
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
