package com.sehoaccountapi.web.controller.books;

import com.sehoaccountapi.config.RestPage;
import com.sehoaccountapi.repository.user.userDetails.CustomUserDetails;
import com.sehoaccountapi.service.books.BookService;
import com.sehoaccountapi.web.dto.books.BookRequest;
import com.sehoaccountapi.web.dto.books.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @GetMapping("/user")
    public ResponseEntity<BookResponse> getBookByUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity.ok(bookService.getBookByUser(customUserDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(bookService.getBookById(customUserDetails.getId(), id));
    }

//    @PostMapping
//    public ResponseEntity<BookResponse> addBook(@AuthenticationPrincipal CustomUserDetails customUserDetails,@RequestBody BookRequest bookRequest) {
//        return ResponseEntity.ok(bookService.addBook(customUserDetails.getId(), bookRequest));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> modifyBook(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable(name = "id") Long bookId, @RequestBody BookRequest bookRequest) {
        return ResponseEntity.ok(bookService.updateBookById(customUserDetails.getId(), bookId, bookRequest));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBookById(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable(name = "id") Long bookId) {
        bookService.deleteBookById(customUserDetails.getId(), bookId);
        return ResponseEntity.ok().build();
    }
}
