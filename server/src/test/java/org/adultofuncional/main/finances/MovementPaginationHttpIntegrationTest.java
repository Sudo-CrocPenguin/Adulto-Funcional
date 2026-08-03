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
 * Verifica filtros, orden y límites SQL del listado de movimientos.
 *
 * <p><strong>Qué es:</strong> una prueba de integración del contrato paginado
 * definido por el ADR 0005.</p>
 *
 * <p><strong>Para qué sirve:</strong> impide regresar al filtrado en memoria,
 * valida los metadatos públicos y protege los límites del endpoint.</p>
 *
 * <p><strong>Cómo funciona:</strong> registra cinco movimientos por HTTP y
 * consulta páginas, combinaciones de filtros y parámetros inválidos contra
 * MariaDB real.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MovementPaginationHttpIntegrationTest extends MariaDbIntegrationTestSupport {

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
    account.setAccountLastNames("Paginación");
    account.setAccountEmail("movement-page-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    account = accountRepository.saveAndFlush(account);
    token = jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
    today = LocalDate.now();

    createMovement("INCOME", "50.00", today.minusDays(4), "Nómina");
    createMovement("EXPENSE", "10.00", today.minusDays(3), "Compra mercado");
    createMovement("EXPENSE", "30.00", today.minusDays(2), "Compra hogar");
    createMovement("EXPENSE", "20.00", today.minusDays(1), "Transporte");
    createMovement("INCOME", "40.00", today, "Reembolso");
  }

  @Test
  void returnsBoundedPagesWithMetadataAndDeterministicOrder() throws Exception {
    mockMvc.perform(authorized(get("/api/finances/movements")
            .queryParam("page", "0")
            .queryParam("size", "2")
            .queryParam("sortBy", "amount")
            .queryParam("sortDirection", "ASC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].amount").value(10.00))
        .andExpect(jsonPath("$.data[1].amount").value(20.00))
        .andExpect(jsonPath("$.page.number").value(0))
        .andExpect(jsonPath("$.page.size").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(5))
        .andExpect(jsonPath("$.page.totalPages").value(3))
        .andExpect(jsonPath("$.page.hasNext").value(true))
        .andExpect(jsonPath("$.page.hasPrevious").value(false));

    mockMvc.perform(authorized(get("/api/finances/movements")
            .queryParam("page", "1")
            .queryParam("size", "2")
            .queryParam("sortBy", "amount")
            .queryParam("sortDirection", "ASC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].amount").value(30.00))
        .andExpect(jsonPath("$.data[1].amount").value(40.00))
        .andExpect(jsonPath("$.page.hasPrevious").value(true));
  }

  @Test
  void appliesCombinedInclusiveFiltersInTheDatabaseQuery() throws Exception {
    mockMvc.perform(authorized(get("/api/finances/movements")
            .queryParam("startDate", today.minusDays(3).toString())
            .queryParam("endDate", today.minusDays(2).toString())
            .queryParam("movementType", "EXPENSE")
            .queryParam("searchTerm", "COMPRA")
            .queryParam("sortBy", "movementDate")
            .queryParam("sortDirection", "ASC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].description").value("Compra mercado"))
        .andExpect(jsonPath("$.data[1].description").value("Compra hogar"))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }

  @Test
  void rejectsInvalidRangesLimitsAndSorts() throws Exception {
    expectInvalid(get("/api/finances/movements")
        .queryParam("startDate", today.toString())
        .queryParam("endDate", today.minusDays(1).toString()));
    expectInvalid(get("/api/finances/movements").queryParam("page", "-1"));
    expectInvalid(get("/api/finances/movements").queryParam("size", "101"));
    expectInvalid(get("/api/finances/movements").queryParam("sortBy", "description"));
    expectInvalid(get("/api/finances/movements").queryParam("sortDirection", "RANDOM"));
  }

  private void createMovement(
      String type,
      String amount,
      LocalDate date,
      String description) throws Exception {
    Map<String, Object> request = Map.of(
        "movementType", type,
        "amount", new BigDecimal(amount),
        "movementDate", date,
        "description", description,
        "categoryId", FINANCE_CATEGORY);
    mockMvc.perform(authorized(post("/api/finances/movements"))
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
