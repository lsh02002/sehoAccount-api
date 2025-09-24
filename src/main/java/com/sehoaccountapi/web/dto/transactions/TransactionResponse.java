package com.sehoaccountapi.web.dto.transactions;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long bookId;
    private String categoryName;
    private BigDecimal amount;
    private String type;
    private String note;
    private String transactionDate;
    private String dedupeKey;
}
