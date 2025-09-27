package com.sehoaccountapi.service.books;

import com.sehoaccountapi.config.RestPage;
import com.sehoaccountapi.repository.book.Book;
import com.sehoaccountapi.repository.book.BookRepository;
import com.sehoaccountapi.repository.user.User;
import com.sehoaccountapi.repository.user.UserRepository;
import com.sehoaccountapi.service.exceptions.BadRequestException;
import com.sehoaccountapi.service.exceptions.NotFoundException;
import com.sehoaccountapi.web.dto.books.BookRequest;
import com.sehoaccountapi.web.dto.books.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public RestPage<BookResponse> getAllBooksByUser(Long userId, Pageable pageable) {
        return new RestPage<>(bookRepository.findByUserId(userId, pageable)
                .map(this::converToBookResponse));
    }

    public BookResponse getBookById(Long userId, Long bookId) {
        return bookRepository.findByUserIdAndId(userId, bookId).map(this::converToBookResponse).orElseThrow(()-> new NotFoundException("해당 가계부 이름을 찾을 수 없습니다.", bookId));
    }

    @Transactional
    public BookResponse addBook(Long userId, BookRequest bookRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new NotFoundException("해당 사용자를 찾을 수 없습니다.", userId));

        if(bookRequest.getName() == null || bookRequest.getName().isEmpty()) {
            throw new BadRequestException("가계부 이름란이 비어있습니다.", null);
        }

        if(bookRequest.getDescription() == null || bookRequest.getDescription().isEmpty()) {
            throw new BadRequestException("상세설명란이 비어있습니다.", null);
        }

        Book savedBook = bookRepository.save(Book.builder()
                .name(bookRequest.getName())
                .user(user)
                .description(bookRequest.getDescription())
                .build());

        return converToBookResponse(savedBook);
    }

    @Transactional
    public BookResponse updateBookById(Long userId, Long bookId, BookRequest bookRequest) {
        Book book = bookRepository.findByUserIdAndId(userId, bookId)
                .orElseThrow(()-> new NotFoundException("해당 가계부를 찾을 수 없습니다.", bookId));

        if(bookRequest.getName() != null && !bookRequest.getName().isEmpty()) {
            book.setName(bookRequest.getName());
        }

        if(bookRequest.getDescription() != null && !bookRequest.getDescription().isEmpty()) {
            book.setDescription(bookRequest.getDescription());
        }

        Book savedBook = bookRepository.save(book);

        return converToBookResponse(savedBook);
    }

    @Transactional
    public void deleteBookById(Long userId, Long bookId) {
        bookRepository.deleteByUserIdAndId(userId, bookId);
    }

    private BookResponse converToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .description(book.getDescription())
                .nickname(book.getUser().getNickname())
                .createdAt(book.getCreatedAt().toString())
                .updatedAt(book.getUpdatedAt().toString())
                .build();
    }
}
