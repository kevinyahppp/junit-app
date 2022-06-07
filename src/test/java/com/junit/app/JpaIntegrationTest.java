package com.junit.app;

import com.junit.app.models.Account;
import com.junit.app.repositories.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Tag("integration_jpa")
@DataJpaTest
public class JpaIntegrationTest {
    @Autowired
    AccountRepository accountRepository;

    @Test
    void findByIdTest() {
        Optional<Account> account = accountRepository.findById(1L);
        Assertions.assertTrue(account.isPresent());
        Assertions.assertEquals("Kevin", account.orElseThrow().getName());
    }

    @Test
    void findByNameTest() {
        Optional<Account> account = accountRepository.findByName("Kevin");
        Assertions.assertTrue(account.isPresent());
        Assertions.assertEquals("Kevin", account.orElseThrow().getName());
        Assertions.assertEquals("1000.00", account.orElseThrow().getBalance().toPlainString());
    }

    @Test
    void findByNameThrowExceptionTest() {
        Optional<Account> account = accountRepository.findByName("");
        Assertions.assertThrows(NoSuchElementException.class, account::orElseThrow);
        Assertions.assertFalse(account.isPresent());
    }

    @Test
    void findByAllTest() {
        List<Account> accounts = accountRepository.findAll();
        Assertions.assertFalse(accounts.isEmpty());
        Assertions.assertEquals(2, accounts.size());
    }

    @Test
    void saveTest() {
        Account account1 = new Account(1L, "Maria", new BigDecimal("3000"));
        Account account2 = accountRepository.save(account1);

        Assertions.assertEquals(account2, account1);
        Assertions.assertEquals("Maria", account2.getName());
        Assertions.assertEquals("3000", account2.getBalance().toPlainString());
    }

    @Test
    void updateTest() {
        Account account1 = new Account(1L, "Maria", new BigDecimal("3000"));
        Account account2 = accountRepository.save(account1);

        Assertions.assertEquals(account2, account1);
        Assertions.assertEquals("Maria", account2.getName());
        Assertions.assertEquals("3000", account2.getBalance().toPlainString());

        account1.setBalance(new BigDecimal("1000"));
        Account account3 = accountRepository.save(account1);

        Assertions.assertEquals(account3, account1);
        Assertions.assertEquals("Maria", account3.getName());
        Assertions.assertEquals("1000", account3.getBalance().toPlainString());
    }

    @Test
    void deleteTest() {
        Account account = accountRepository.findById(2L).orElseThrow();
        Assertions.assertEquals("Brando", account.getName());

        accountRepository.delete(account);

        Assertions.assertThrows(NoSuchElementException.class, () -> accountRepository.findByName("Brando").orElseThrow());
    }
}
