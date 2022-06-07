package com.junit.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junit.app.Data;
import com.junit.app.models.Account;
import com.junit.app.models.TransactionDTO;
import com.junit.app.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Slf4j
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService accountService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void detailTest() throws Exception {
        when(accountService.findById(1L)).thenReturn(Data.createAccount001().orElseThrow());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Kevin"))
                .andExpect(jsonPath("$.balance").value("1000"));

        verify(accountService).findById(1L);
    }

    @Test
    void transferTest() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(new BigDecimal("100"));
        transactionDTO.setOriginAccountId(1L);
        transactionDTO.setDestinationAccountId(2L);
        transactionDTO.setBankId(1L);

        log.info(objectMapper.writeValueAsString(transactionDTO));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("message", "Transfer done successfully");
        response.put("transaction", transactionDTO);

        log.info(objectMapper.writeValueAsString(response));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.message").value("Transfer done successfully"))
                .andExpect(jsonPath("$.transaction.originAccountId").value(transactionDTO.getOriginAccountId()))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void listTest() throws Exception {
        List<Account> accounts = Arrays.asList(Data.createAccount001().orElseThrow(),
                Data.createAccount002().orElseThrow());
        when(accountService.findAll()).thenReturn(accounts);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Kevin"))
                .andExpect(jsonPath("$[1].name").value("Brando"))
                .andExpect(jsonPath("$[0].balance").value("1000"))
                .andExpect(jsonPath("$[1].balance").value("2000"))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(content().json(objectMapper.writeValueAsString(accounts)));
    }

    @Test
    void saveTest() throws Exception {
        Account account = new Account(null, "Karen", new BigDecimal("3000"));
        when(accountService.save(any())).then(invocation -> {
            Account account1 = invocation.getArgument(0);
            account1.setId(3L);
            return account1;
        });

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", Matchers.is(3)))
                .andExpect(jsonPath("$.name", Matchers.is("Karen")))
                .andExpect(jsonPath("$.balance", Matchers.is(3000)));

        verify(accountService).save(any());
    }
}