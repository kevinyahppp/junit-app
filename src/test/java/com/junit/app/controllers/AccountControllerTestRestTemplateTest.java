package com.junit.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junit.app.models.Account;
import com.junit.app.models.TransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration_rt")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTestRestTemplateTest {
    @Autowired
    private TestRestTemplate testRestTemplate;
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        objectMapper =  new ObjectMapper();
    }

    private String createUri(String uri) {
        return "http://localhost:".concat(String.valueOf(port)).concat(uri);
    }

    @Test
    @Order(1)
    void transferTest() throws JsonProcessingException {
        TransactionDTO transactionDTO =  new TransactionDTO();
        transactionDTO.setOriginAccountId(1L);
        transactionDTO.setDestinationAccountId(2L);
        transactionDTO.setBankId(1L);
        transactionDTO.setAmount(new BigDecimal("100"));

        System.out.println("Port: ".concat(String.valueOf(port)));
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(createUri("/api/accounts/transfer"),
                transactionDTO, String.class);
        String json = responseEntity.getBody();
        System.out.println("Json: ".concat(json));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertNotNull(json);
        assertTrue(json.contains("Transfer done successfully"));

        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals("Transfer done successfully", jsonNode.path("message").asText());
        assertEquals(LocalDate.now().toString(), jsonNode.path("date").asText());
        assertEquals("100", jsonNode.path("transaction").path("amount").asText());
        assertEquals(1L, jsonNode.path("transaction").path("originAccountId").asLong());

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("message", "Transfer done successfully");
        response.put("transaction", transactionDTO);

        assertEquals(objectMapper.writeValueAsString(response), json);
    }

    @Test
    @Order(2)
    void detailTest() {
        ResponseEntity<Account> response = testRestTemplate.getForEntity(createUri("/api/accounts/1"),
                Account.class);
        Account account = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals("Kevin", account.getName());
        assertEquals("900.00", account.getBalance().toPlainString());
        assertEquals(new Account(1L, "Kevin", new BigDecimal("900.00")), account);
    }

    @Test
    @Order(3)
    void listTest() throws JsonProcessingException {
        ResponseEntity<Account[]> response = testRestTemplate.getForEntity(createUri("/api/accounts"), Account[].class);
        List<Account> accounts = Arrays.asList(response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertEquals(2, accounts.size());
        assertEquals(1L, accounts.get(0).getId());
        assertEquals("Kevin", accounts.get(0).getName());
        assertEquals("900.00", accounts.get(0).getBalance().toPlainString());
        assertEquals(2L, accounts.get(1).getId());
        assertEquals("Brando", accounts.get(1).getName());
        assertEquals("2100.00", accounts.get(1).getBalance().toPlainString());

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(accounts));
        assertEquals(1L, jsonNode.get(0).path("id").asLong());
        assertEquals("Kevin", jsonNode.get(0).path("name").asText());
        assertEquals("900.0", jsonNode.get(0).path("balance").asText());
        assertEquals(2L, jsonNode.get(1).path("id").asLong());
        assertEquals("Brando", jsonNode.get(1).path("name").asText());
        assertEquals("2100.0", jsonNode.get(1).path("balance").asText());
    }

    @Test
    @Order(4)
    void saveTest() {
        Account account = new Account(null, "Karen", new BigDecimal("3000"));
        ResponseEntity<Account> response = testRestTemplate.postForEntity(createUri("/api/accounts"),
                account, Account.class);

        Account account1 = response.getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        assertNotNull(account1);
        assertEquals(3L, account1.getId());
        assertEquals("Karen", account1.getName());
        assertEquals("3000", account1.getBalance().toPlainString());
    }

    @Test
    @Order(5)
    void deleteTest() {
        ResponseEntity<Account[]> response = testRestTemplate.getForEntity(createUri("/api/accounts"), Account[].class);
        List<Account> accounts = Arrays.asList(response.getBody());
        assertEquals(3, accounts.size());

        Map<String, Long> pathVariables = new HashMap<>();
        pathVariables.put("id", 3L);

//        testRestTemplate.delete(createUri("/api/accounts/3"));
        ResponseEntity<Void> exchange = testRestTemplate.exchange(createUri("/api/accounts/{id}"),
                HttpMethod.DELETE, null, Void.class, pathVariables);
        assertEquals(HttpStatus.NO_CONTENT, exchange.getStatusCode());

        response = testRestTemplate.getForEntity(createUri("/api/accounts"), Account[].class);
        accounts = Arrays.asList(response.getBody());
        assertEquals(2, accounts.size());
        assertFalse(exchange.hasBody());

        ResponseEntity<Account> response1 = testRestTemplate.getForEntity(createUri("/api/accounts/3"),
                Account.class);
        assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
        assertFalse(response1.hasBody());
    }
}
