package com.junit.app;

import com.junit.app.exceptions.NotEnoughMoney;
import com.junit.app.models.Account;
import com.junit.app.models.Bank;
import com.junit.app.repositories.AccountRepository;
import com.junit.app.repositories.BankRepository;
import com.junit.app.services.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class JunitAppApplicationTests {

	@MockBean
	AccountRepository accountRepository;
	@MockBean
	BankRepository bankRepository;
	@Autowired
	AccountServiceImpl accountService;

	@BeforeEach
	void setUp() {
//		accountService = new AccountServiceImpl(accountRepository, bankRepository);
//		Data.ACCOUNT_001.setBalance(new BigDecimal("1000"));
//		Data.ACCOUNT_002.setBalance(new BigDecimal("2000"));
//		Data.BANK.setTotalTransfers(0);
	}

	@Test
	void contextLoads() {
		when(accountRepository.findById(1L)).thenReturn(Data.createAccount001());
		when(accountRepository.findById(2L)).thenReturn(Data.createAccount002());
		when(bankRepository.findById(1L)).thenReturn(Data.createBank());

		BigDecimal originBalance = accountService.reviewBalance(1L);
		BigDecimal destinationBalance = accountService.reviewBalance(2L);

		assertEquals("1000", originBalance.toPlainString());
		assertEquals("2000", destinationBalance.toPlainString());

		accountService.transfer(1L, 2L, new BigDecimal("100"), 1L);

		originBalance = accountService.reviewBalance(1L);
		destinationBalance = accountService.reviewBalance(2L);

		assertEquals("900", originBalance.toPlainString());
		assertEquals("2100", destinationBalance.toPlainString());

		int totalTransfers = accountService.reviewTotalTransfers(1L);
		assertEquals(1, totalTransfers);

		verify(accountRepository, times(3)).findById(1L);
		verify(accountRepository, times(3)).findById(2L);
		verify(accountRepository, times(2)).save(any(Account.class));

		verify(bankRepository, times(2)).findById(1L);
		verify(bankRepository).save(any(Bank.class));

		verify(accountRepository, never()).findAll();
		verify(accountRepository, times(6)).findById(any(Long.class));
	}

	@Test
	void contextLoads2() {
		when(accountRepository.findById(1L)).thenReturn(Data.createAccount001());
		when(accountRepository.findById(2L)).thenReturn(Data.createAccount002());
		when(bankRepository.findById(1L)).thenReturn(Data.createBank());

		BigDecimal originBalance = accountService.reviewBalance(1L);
		BigDecimal destinationBalance = accountService.reviewBalance(2L);

		assertEquals("1000", originBalance.toPlainString());
		assertEquals("2000", destinationBalance.toPlainString());

		assertThrows(NotEnoughMoney.class, () -> {
			accountService.transfer(1L, 2L, new BigDecimal("1200"), 1L);
		});

		originBalance = accountService.reviewBalance(1L);
		destinationBalance = accountService.reviewBalance(2L);

		assertEquals("1000", originBalance.toPlainString());
		assertEquals("2000", destinationBalance.toPlainString());

		int totalTransfers = accountService.reviewTotalTransfers(1L);
		assertEquals(0, totalTransfers);

		verify(accountRepository, times(3)).findById(1L);
		verify(accountRepository, times(2)).findById(2L);
		verify(accountRepository, never()).save(any(Account.class));

		verify(bankRepository, times(1)).findById(1L);
		verify(bankRepository, never()).save(any(Bank.class));

		verify(accountRepository, never()).findAll();
		verify(accountRepository, times(5)).findById(any(Long.class));
	}

	@Test
	void contextLoads3() {
		when(accountRepository.findById(1L)).thenReturn(Data.createAccount001());

		Account account1 = accountService.findById(1L);
		Account account2 = accountService.findById(1L);

		assertSame(account1, account2);
		assertTrue(account1 == account2);
		assertEquals("Kevin", account1.getName());
		assertEquals("Kevin", account2.getName());

		verify(accountRepository, times(2)).findById(1L);
	}

	@Test
	void findAllTest() {
		List<Account> accounts = Arrays.asList(Data.createAccount001().orElseThrow(), Data.createAccount002().orElseThrow());
		when(accountRepository.findAll()).thenReturn(accounts);

		List<Account> accounts1 = accountService.findAll();

		assertEquals(accounts, accounts1);
		assertFalse(accounts1.isEmpty());
		assertEquals(2, accounts1.size());
		assertTrue(accounts1.contains(Data.createAccount001().orElseThrow()));

		verify(accountRepository).findAll();
	}

	@Test
	void saveTest() {
		Account account = new Account(null, "Karen", new BigDecimal("3000"));
		when(accountRepository.save(any())).then(invocation -> {
			Account account1 = invocation.getArgument(0);
			account1.setId(3L);
			return account1;
		});

		Account account1 = accountService.save(account);

		assertEquals(account, account1);
		assertEquals("Karen", account1.getName());
		assertEquals("3000", account1.getBalance().toPlainString());

		verify(accountRepository).save(any());
	}
}
