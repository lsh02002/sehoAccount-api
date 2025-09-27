package com.sehoaccountapi.web.controller.transaction;

import com.sehoaccountapi.config.RestPage;
import com.sehoaccountapi.repository.user.userDetails.CustomUserDetails;
import com.sehoaccountapi.service.transaction.TransactionService;
import com.sehoaccountapi.web.dto.transactions.TransactionRequest;
import com.sehoaccountapi.web.dto.transactions.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/books/{bookId}")
    public ResponseEntity<RestPage<TransactionResponse>> getAllByUserIdAndBookId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable(name = "bookId") Long bookId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.getAllByUserIdAndBookId(customUserDetails.getId(), bookId, pageable));
    }

    @GetMapping("/{bookId}/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionByBookIdAndId(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable(name = "bookId") Long bookId, @PathVariable(name = "transactionId") Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(customUserDetails.getId(), bookId, transactionId));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.createTransaction(customUserDetails.getId(), transactionRequest));
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable(name = "transactionId") Long transactionId, @RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.updateTransaction(customUserDetails.getId(), transactionId, transactionRequest));
    }
}
