package com.junit.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junit.app.models.Account;
import com.junit.app.models.TransactionDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration_wc")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerWebTestClientTest {
    @Autowired
    private WebTestClient webTestClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    void transferTest() throws JsonProcessingException {
        TransactionDTO transactionDTO =  new TransactionDTO();
        transactionDTO.setOriginAccountId(1L);
        transactionDTO.setDestinationAccountId(2L);
        transactionDTO.setBankId(1L);
        transactionDTO.setAmount(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("message", "Transfer done successfully");
        response.put("transaction", transactionDTO);

        webTestClient.post().uri("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transactionDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(entityExchangeResult -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(entityExchangeResult.getResponseBody());
                        assertEquals("Transfer done successfully", jsonNode.path("message").asText());
                        assertEquals(1L, jsonNode.path("transaction").path("originAccountId").asInt());
                        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
                        assertEquals("100", jsonNode.path("transaction").path("amount").asText());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.message").value(Matchers.is("Transfer done successfully"))
                .jsonPath("$.message").value(o -> assertEquals("Transfer done successfully", o))
                .jsonPath("$.message").isEqualTo("Transfer done successfully")
                .jsonPath("$.transaction.originAccountId").isEqualTo(transactionDTO.getOriginAccountId())
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
                .json(objectMapper.writeValueAsString(response));
        ;

        ;
    }

    @Test
    @Order(2)
    void detailTest() throws JsonProcessingException {
        Account account = new Account(1L, "Kevin", new BigDecimal("900"));
        webTestClient.get().uri("/api/accounts/1")
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Kevin")
                .jsonPath("$.balance").isEqualTo(900)
                .json(objectMapper.writeValueAsString(account));
    }

    @Test
    @Order(3)
    void detailTest2() {
        webTestClient.get().uri("/api/accounts/2")
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .consumeWith(result -> {
                    Account account = result.getResponseBody();
                    assertEquals("Brando", account.getName());
                    assertEquals("2100.00", account.getBalance().toPlainString());
                });
    }

    @Test
    @Order(4)
    void listTest() {
        webTestClient.get().uri("/api/accounts").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Kevin")
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].balance").isEqualTo(900)
                .jsonPath("$[1].name").isEqualTo("Brando")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].balance").isEqualTo(2100)
                .jsonPath("$").isArray()
                .jsonPath("$").value(Matchers.hasSize(2));
    }

    @Test
    @Order(5)
    void listTest2() {
        webTestClient.get().uri("/api/accounts").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Account.class)
                .consumeWith(result -> {
                    List<Account> accounts = result.getResponseBody();
                    assertNotNull(accounts);
                    assertEquals(2, accounts.size());
                    assertEquals(1L, accounts.get(0).getId());
                    assertEquals("Kevin", accounts.get(0).getName());
                    assertEquals(900, accounts.get(0).getBalance().intValue());
                    assertEquals(2L, accounts.get(1).getId());
                    assertEquals("Brando", accounts.get(1).getName());
                    assertEquals("2100.0", accounts.get(1).getBalance().toPlainString());
                })
                .hasSize(2)
                .value(Matchers.hasSize(2))
        ;
    }

    @Test
    @Order(6)
    void saveTest() {
        Account account = new Account(null, "Karen", new BigDecimal("3000"));
        webTestClient.post().uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(account)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(3)
                .jsonPath("$.name").isEqualTo("Karen")
                .jsonPath("$.name").value(Matchers.is("Karen"))
                .jsonPath("$.balance").isEqualTo(3000);
    }

    @Test
    @Order(7)
    void saveTest2() {
        Account account = new Account(null, "Karen", new BigDecimal("3000"));
        webTestClient.post().uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(account)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Account.class)
                .consumeWith(result ->{
                    Account account1 = result.getResponseBody();
                    assertNotNull(account1);
                    assertEquals(4L, account1.getId());
                    assertEquals("Karen", account1.getName());
                    assertEquals("3000", account1.getBalance().toPlainString());
                });
    }

    @Test
    @Order(8)
    void deleteByIdTest() {
        webTestClient.get().uri("/api/accounts").exchange()
                .expectStatus().isOk()
                .expectBodyList(Account.class)
                .hasSize(4);

        webTestClient.delete().uri("/api/accounts/3")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webTestClient.get().uri("/api/accounts").exchange()
                .expectStatus().isOk()
                .expectBodyList(Account.class)
                .hasSize(3);

        webTestClient.get().uri("/api/accounts/3").exchange()
//                .expectStatus().is5xxServerError();
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}
