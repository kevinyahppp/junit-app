package com.junit.app.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {
    private Long originAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private Long bankId;
}
