package org.adultofuncional.main.finances;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contrato HTTP de paginación y filtros SQL para gastos fijos.
 *
 * <p><strong>Qué es:</strong> una prueba integral del listado conforme al ADR
 * 0005.</p>
 *
 * <p><strong>Para qué sirve:</strong> protege los límites de consumo, el orden
 * reproducible, los filtros combinables y los metadatos públicos.</p>
 *
 * <p><strong>Cómo funciona:</strong> registra cinco gastos por HTTP y consulta
 * MariaDB con páginas, filtros y parámetros negativos.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FixedExpensePaginationHttpIntegrationTest extends MariaDbIntegrationTestSupport {

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
  LocalDate today;

  @BeforeEach
  void setUpData() throws Exception {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Gastos");
    account.setAccountEmail("expense-page-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    account = accountRepository.saveAndFlush(account);
    token = jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
    today = LocalDate.now();

    createExpense("Alpha Streaming", "10.00", "ACTIVE", 5);
    createExpense("Beta Gimnasio", "20.00", "INACTIVE", 4);
    createExpense("Alpha Nube", "30.00", "ACTIVE", 3);
    createExpense("Arriendo", "40.00", "ACTIVE", 2);
    createExpense("Seguro", "50.00", "INACTIVE", 1);
  }

  @Test
  void returnsOrderedBoundedPagesAndMetadata() throws Exception {
    mockMvc.perform(authorized(get("/api/finances/fixed-expenses")
            .queryParam("page", "0")
            .queryParam("size", "2")
            .queryParam("sortBy", "amount")
            .queryParam("sortDirection", "DESC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].amount").value(50.00))
        .andExpect(jsonPath("$.data[1].amount").value(40.00))
        .andExpect(jsonPath("$.page.totalElements").value(5))
        .andExpect(jsonPath("$.page.totalPages").value(3))
        .andExpect(jsonPath("$.page.hasNext").value(true));
  }

  @Test
  void combinesStatusAndNameFilters() throws Exception {
    mockMvc.perform(authorized(get("/api/finances/fixed-expenses")
            .queryParam("status", "ACTIVE")
            .queryParam("searchTerm", "ALPHA")
            .queryParam("sortBy", "name")
            .queryParam("sortDirection", "ASC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].name").value("Alpha Nube"))
        .andExpect(jsonPath("$.data[1].name").value("Alpha Streaming"))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }

  @Test
  void rejectsInvalidPageAndSortParameters() throws Exception {
    expectInvalid(get("/api/finances/fixed-expenses").queryParam("page", "-1"));
    expectInvalid(get("/api/finances/fixed-expenses").queryParam("size", "0"));
    expectInvalid(get("/api/finances/fixed-expenses").queryParam("size", "101"));
    expectInvalid(get("/api/finances/fixed-expenses").queryParam("sortBy", "category"));
    expectInvalid(get("/api/finances/fixed-expenses").queryParam("sortDirection", "RANDOM"));
  }

  private void createExpense(
      String name,
      String amount,
      String status,
      long dueInDays) throws Exception {
    Map<String, Object> request = Map.of(
        "name", name,
        "frequency", "MONTHLY",
        "amount", new BigDecimal(amount),
        "status", status,
        "nextDueDate", today.plusDays(dueInDays),
        "categoryId", FINANCE_CATEGORY);
    mockMvc.perform(authorized(post("/api/finances/fixed-expenses"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request)))
        .andExpect(status().isCreated());
  }

  private void expectInvalid(MockHttpServletRequestBuilder request) throws Exception {
    mockMvc.perform(authorized(request))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETER_INVALID"));
  }

  private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }
}
