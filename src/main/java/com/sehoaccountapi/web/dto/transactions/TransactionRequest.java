package com.sehoaccountapi.web.dto.transactions;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private Long bookId;
    private Long categoryId;
    private String transactionDate;
    private BigDecimal amount;
    private String type;
    private String note;
    private String dedupeKey;
}
