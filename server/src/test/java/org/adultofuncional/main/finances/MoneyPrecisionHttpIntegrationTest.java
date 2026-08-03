package org.adultofuncional.main.finances;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifica la precisión monetaria pública de movimientos y gastos fijos.
 *
 * <p><strong>Qué es:</strong> una prueba de contrato HTTP respaldada por
 * MariaDB y el esquema real {@code DECIMAL(10,2)}.</p>
 *
 * <p><strong>Para qué sirve:</strong> garantiza que importes imposibles de
 * persistir no lleguen a la base de datos ni produzcan errores 500.</p>
 *
 * <p><strong>Cómo funciona:</strong> prueba los límites válidos y rechaza por
 * creación y PATCH tanto fracciones excesivas como más de ocho enteros.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MoneyPrecisionHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final UUID FINANCE_CATEGORY =
      UUID.fromString("01988e6b-0c00-7000-8000-000000000001");

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  String token;

  @BeforeEach
  void setUpAccount() {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Finanzas");
    account.setAccountEmail("money-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    account = accountRepository.saveAndFlush(account);
    token = jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  @Test
  void acceptsTheDecimalColumnBoundaries() throws Exception {
    Map<String, Object> movement = movementRequest(new BigDecimal("99999999.99"));
    mockMvc.perform(authorized(post("/api/finances/movements"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(movement)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.amount").value(99999999.99));

    Map<String, Object> fixedExpense = fixedExpenseRequest(new BigDecimal("0.01"));
    mockMvc.perform(authorized(post("/api/finances/fixed-expenses"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(fixedExpense)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.amount").value(0.01));
  }

  @Test
  void rejectsInvalidPrecisionDuringCreation() throws Exception {
    expectValidationFailure(
        post("/api/finances/movements"),
        movementRequest(new BigDecimal("10.001")));
    expectValidationFailure(
        post("/api/finances/movements"),
        movementRequest(new BigDecimal("100000000.00")));
    expectValidationFailure(
        post("/api/finances/fixed-expenses"),
        fixedExpenseRequest(new BigDecimal("10.001")));
    expectValidationFailure(
        post("/api/finances/fixed-expenses"),
        fixedExpenseRequest(new BigDecimal("100000000.00")));
  }

  @Test
  void rejectsInvalidPrecisionDuringPartialUpdates() throws Exception {
    UUID movementId = createAndReturnId(
        "/api/finances/movements",
        movementRequest(new BigDecimal("10.00")));
    UUID expenseId = createAndReturnId(
        "/api/finances/fixed-expenses",
        fixedExpenseRequest(new BigDecimal("10.00")));

    expectValidationFailure(
        patch("/api/finances/movements/{id}", movementId),
        Map.of("amount", new BigDecimal("0.001")));
    expectValidationFailure(
        patch("/api/finances/fixed-expenses/{id}", expenseId),
        Map.of("amount", new BigDecimal("100000000.00")));
  }

  private UUID createAndReturnId(String path, Object request) throws Exception {
    MvcResult result = mockMvc.perform(authorized(post(path))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(request)))
        .andExpect(status().isCreated())
        .andReturn();
    return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsByteArray())
        .path("data")
        .path("id")
        .asText());
  }

  private Map<String, Object> movementRequest(BigDecimal amount) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("movementType", "EXPENSE");
    request.put("amount", amount);
    request.put("movementDate", LocalDate.now());
    request.put("description", "Compra de prueba");
    request.put("categoryId", FINANCE_CATEGORY);
    return request;
  }

  private Map<String, Object> fixedExpenseRequest(BigDecimal amount) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("name", "Suscripción");
    request.put("frequency", "MONTHLY");
    request.put("amount", amount);
    request.put("status", "ACTIVE");
    request.put("nextDueDate", LocalDate.now().plusMonths(1));
    request.put("categoryId", FINANCE_CATEGORY);
    return request;
  }

  private void expectValidationFailure(
      MockHttpServletRequestBuilder request,
      Object body) throws Exception {
    mockMvc.perform(authorized(request)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }

  private byte[] json(Object value) throws Exception {
    return objectMapper.writeValueAsBytes(value);
  }
}
