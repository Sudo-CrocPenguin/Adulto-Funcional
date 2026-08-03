package org.adultofuncional.main.finances;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifica el contrato HTTP de categorías SYSTEM/PERSONAL contra MariaDB.
 *
 * <p><strong>Qué es:</strong> una prueba de integración que atraviesa JWT,
 * controlador, aplicación, adaptador JPA, restricciones de V6 y MariaDB.</p>
 *
 * <p><strong>Para qué sirve:</strong> evita regresiones de ownership,
 * normalización, inmutabilidad del catálogo y compatibilidad entre el tipo de
 * categoría y el módulo que la consume.</p>
 *
 * <p><strong>Cómo funciona:</strong> crea dos cuentas con tokens distintos,
 * administra categorías por HTTP y comprueba tanto recorridos válidos como
 * intentos cruzados y conflictos de integridad.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryOwnershipHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final UUID SYSTEM_FINANCE_CATEGORY =
      UUID.fromString("01988e6b-0c00-7000-8000-000000000001");

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  String ownerToken;
  String foreignToken;

  @BeforeEach
  void setUpAccounts() {
    ownerToken = tokenFor(persistAccount("category-owner"));
    foreignToken = tokenFor(persistAccount("category-foreign"));
  }

  @Test
  void listsSystemAndOwnCategoriesWithoutExposingForeignOnes() throws Exception {
    UUID ownCategory = createCategory(ownerToken, "Mascotas", "FINANCES");
    UUID foreignCategory = createCategory(foreignToken, "Privada ajena", "FINANCES");

    MvcResult result = mockMvc.perform(authorized(
            get("/api/finances/categories").queryParam("type", "FINANCES"),
            ownerToken))
        .andExpect(status().isOk())
        .andReturn();

    List<UUID> visibleIds = objectMapper.readTree(result.getResponse().getContentAsByteArray())
        .path("data")
        .findValuesAsText("id")
        .stream()
        .map(UUID::fromString)
        .toList();
    assertThat(visibleIds)
        .contains(SYSTEM_FINANCE_CATEGORY, ownCategory)
        .doesNotContain(foreignCategory);
  }

  @Test
  void protectsSystemAndForeignCategoriesFromMutation() throws Exception {
    UUID ownCategory = createCategory(ownerToken, "Formación", "FINANCES");
    UUID foreignCategory = createCategory(foreignToken, "Solo ajena", "FINANCES");

    mockMvc.perform(authorized(patch("/api/finances/categories/{id}", foreignCategory), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", "Intrusión"))))
        .andExpect(status().isNotFound());
    mockMvc.perform(authorized(delete("/api/finances/categories/{id}", foreignCategory), ownerToken))
        .andExpect(status().isNotFound());
    mockMvc.perform(authorized(delete("/api/finances/categories/{id}", SYSTEM_FINANCE_CATEGORY), ownerToken))
        .andExpect(status().isNotFound());

    mockMvc.perform(authorized(patch("/api/finances/categories/{id}", ownCategory), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("type", "AGENDA"))))
        .andExpect(status().isBadRequest());
    mockMvc.perform(authorized(get("/api/finances/categories/{id}", ownCategory), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.type").value("FINANCES"))
        .andExpect(jsonPath("$.data.scope").value("PERSONAL"));
  }

  @Test
  void rejectsCanonicalDuplicates() throws Exception {
    createCategory(ownerToken, "Café Hogar", "FINANCES");
    mockMvc.perform(authorized(post("/api/finances/categories"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", "CAFÉ HOGAR", "type", "FINANCES"))))
        .andExpect(status().isConflict());
  }

  @Test
  void rejectsCategoriesFromAnotherModule() throws Exception {
    UUID agendaCategory = createCategory(ownerToken, "Agenda propia", "AGENDA");
    mockMvc.perform(authorized(post("/api/finances/movements"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(movementRequest(agendaCategory))))
        .andExpect(status().isNotFound());

    UUID financeCategory = createCategory(ownerToken, "Finanzas propias", "FINANCES");
    mockMvc.perform(authorized(post("/api/agenda/events"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(eventRequest(financeCategory))))
        .andExpect(status().isNotFound());
  }

  @Test
  void returnsConflictWhenDeletingAReferencedPersonalCategory() throws Exception {
    UUID categoryId = createCategory(ownerToken, "Compras recurrentes", "FINANCES");
    mockMvc.perform(authorized(post("/api/finances/movements"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(movementRequest(categoryId))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.category.id").value(categoryId.toString()))
        .andExpect(jsonPath("$.data.category.scope").value("PERSONAL"));

    mockMvc.perform(authorized(delete("/api/finances/categories/{id}", categoryId), ownerToken))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_CONFLICT"));
  }

  private UUID createCategory(String token, String name, String type) throws Exception {
    MvcResult result = mockMvc.perform(authorized(post("/api/finances/categories"), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("name", name, "type", type))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.scope").value("PERSONAL"))
        .andReturn();
    return responseId(result);
  }

  private Map<String, Object> movementRequest(UUID categoryId) {
    return Map.of(
        "movementType", "EXPENSE",
        "amount", new BigDecimal("100.00"),
        "movementDate", LocalDate.of(2026, 8, 3),
        "description", "Movimiento de prueba",
        "categoryId", categoryId);
  }

  private Map<String, Object> eventRequest(UUID categoryId) {
    LocalDateTime start = LocalDateTime.of(2026, 8, 4, 9, 0);
    return Map.ofEntries(
        Map.entry("title", "Evento de prueba"),
        Map.entry("priority", "Media"),
        Map.entry("eventDate", start.toLocalDate()),
        Map.entry("frequency", 0),
        Map.entry("reminder", start.minusHours(1)),
        Map.entry("startHour", start),
        Map.entry("endHour", start.plusHours(1)),
        Map.entry("description", "Evento inválido por categoría"),
        Map.entry("status", "Pendiente"),
        Map.entry("categoryId", categoryId));
  }

  private AccountEntity persistAccount(String prefix) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Categorías");
    account.setAccountEmail(prefix + "-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    return accountRepository.saveAndFlush(account);
  }

  private String tokenFor(AccountEntity account) {
    return jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  private MockHttpServletRequestBuilder authorized(
      MockHttpServletRequestBuilder request,
      String token) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }

  private byte[] json(Object value) throws Exception {
    return objectMapper.writeValueAsBytes(value);
  }

  private UUID responseId(MvcResult result) throws Exception {
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return UUID.fromString(body.path("data").path("id").asText());
  }
}
