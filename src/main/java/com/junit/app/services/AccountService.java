package com.junit.app.services;

import com.junit.app.models.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<Account> findAll();
    Account findById(Long id);
    Account save(Account account);
    void deleteById(Long id);
    int reviewTotalTransfers(Long bankId);
    BigDecimal reviewBalance(Long accountId);
    void transfer(Long originAccountNumber, Long destinationAccountNumber, BigDecimal amount, Long bankId);
}
