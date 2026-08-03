package org.adultofuncional.main;

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
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifica la frontera HTTP de ownership con dos identidades JWT reales.
 *
 * <p><strong>Qué prueba:</strong> que el identificador de cuenta firmado en el
 * JWT atraviese correctamente el filtro, el principal autenticado, el
 * controlador, el caso de uso y la consulta delimitada de MariaDB.</p>
 *
 * <p><strong>Por qué existe:</strong> las pruebas unitarias y de repositorio
 * protegen capas aisladas, pero no detectarían una regresión en el enlace
 * entre seguridad HTTP y aplicación.</p>
 *
 * <p><strong>Cómo funciona:</strong> crea dos cuentas dentro de una transacción,
 * firma un JWT para cada una y crea los recursos del propietario mediante la
 * API. La segunda cuenta intenta consultarlos, modificarlos o eliminarlos. Los
 * cambios se revierten después de cada prueba.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResourceOwnershipHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final UUID FINANCE_CATEGORY_ID =
      UUID.fromString("01988e6b-0c00-7000-8000-000000000001");
  private static final UUID AGENDA_CATEGORY_ID =
      UUID.fromString("01988e6b-0c00-7000-8000-000000000009");
  private static final String MASTER_KEY = "MasterKey-E2E-2026";

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  MasterKeySessionService masterKeySessionService;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  AccountEntity owner;
  AccountEntity foreignAccount;
  String ownerToken;
  String foreignToken;

  @BeforeEach
  void setUpAccounts() {
    String masterKeyHash = passwordEncoder.encode(MASTER_KEY);
    owner = persistAccount("owner-" + UUID.randomUUID() + "@example.com", masterKeyHash);
    foreignAccount = persistAccount("foreign-" + UUID.randomUUID() + "@example.com", masterKeyHash);
    ownerToken = tokenFor(owner);
    foreignToken = tokenFor(foreignAccount);
  }

  @AfterEach
  void clearMasterKeySessions() {
    masterKeySessionService.clearAll(owner.getAccountId());
    masterKeySessionService.clearAll(foreignAccount.getAccountId());
  }

  @Test
  void rejectsForeignMovementGetPatchAndDeleteWithoutChangingOwnerResource() throws Exception {
    UUID movementId = createMovementAsOwner();

    mockMvc.perform(authorized(get("/api/finances/movements/{id}", movementId), foreignToken))
        .andExpect(status().isNotFound());
    expectOwnerMovementUnchanged(movementId);

    mockMvc.perform(authorized(patch("/api/finances/movements/{id}", movementId), foreignToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("description", "Intento ajeno"))))
        .andExpect(status().isNotFound());
    expectOwnerMovementUnchanged(movementId);

    mockMvc.perform(authorized(delete("/api/finances/movements/{id}", movementId), foreignToken))
        .andExpect(status().isNotFound());
    expectOwnerMovementUnchanged(movementId);

    mockMvc.perform(authorized(delete("/api/finances/movements/{id}", movementId), ownerToken))
        .andExpect(status().isOk());
    mockMvc.perform(authorized(get("/api/finances/movements/{id}", movementId), ownerToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void rejectsForeignEventDeleteAndPreservesOwnerResource() throws Exception {
    UUID eventId = createEventAsOwner();

    mockMvc.perform(authorized(delete("/api/agenda/events/{id}", eventId), foreignToken))
        .andExpect(status().isNotFound());
    mockMvc.perform(authorized(get("/api/agenda/events/{id}", eventId), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("Evento del propietario"));

    mockMvc.perform(authorized(delete("/api/agenda/events/{id}", eventId), ownerToken))
        .andExpect(status().isOk());
    mockMvc.perform(authorized(get("/api/agenda/events/{id}", eventId), ownerToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void enforcesMasterKeyAndOwnershipWhenDeletingCredentials() throws Exception {
    verifyMasterKey(ownerToken);
    UUID credentialId = createCredentialAsOwner();
    masterKeySessionService.clear(
        owner.getAccountId(),
        jwtService.extractSessionId(ownerToken));

    mockMvc.perform(authorized(delete("/api/security/passwords/{id}", credentialId), ownerToken))
        .andExpect(status().isForbidden());

    verifyMasterKey(ownerToken);
    mockMvc.perform(authorized(get("/api/security/passwords/{id}", credentialId), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.applicationName").value("Credencial del propietario"));

    verifyMasterKey(foreignToken);
    mockMvc.perform(authorized(delete("/api/security/passwords/{id}", credentialId), foreignToken))
        .andExpect(status().isNotFound());
    mockMvc.perform(authorized(get("/api/security/passwords/{id}", credentialId), ownerToken))
        .andExpect(status().isOk());

    mockMvc.perform(authorized(delete("/api/security/passwords/{id}", credentialId), ownerToken))
        .andExpect(status().isOk());
    mockMvc.perform(authorized(get("/api/security/passwords/{id}", credentialId), ownerToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void masterKeyUnlockIsRestrictedToTheJwtSessionThatVerifiedIt() throws Exception {
    String secondDeviceToken = tokenFor(owner);

    verifyMasterKey(ownerToken);

    mockMvc.perform(authorized(get("/api/security/passwords"), ownerToken))
        .andExpect(status().isOk());
    mockMvc.perform(authorized(get("/api/security/passwords"), secondDeviceToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MASTER_KEY_REQUIRED"));
  }

  private UUID createMovementAsOwner() throws Exception {
    Map<String, Object> request = Map.of(
        "movementType", "EXPENSE",
        "amount", new BigDecimal("25000.00"),
        "movementDate", LocalDate.of(2026, 7, 31),
        "description", "Compra del propietario",
        "categoryId", FINANCE_CATEGORY_ID);

    MvcResult result = mockMvc.perform(authorized(post("/api/finances/movements"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(request)))
        .andExpect(status().isCreated())
        .andReturn();
    return responseId(result);
  }

  private void expectOwnerMovementUnchanged(UUID movementId) throws Exception {
    mockMvc.perform(authorized(get("/api/finances/movements/{id}", movementId), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.description").value("Compra del propietario"));
  }

  private UUID createEventAsOwner() throws Exception {
    LocalDateTime start = LocalDateTime.of(2026, 8, 10, 14, 0);
    Map<String, Object> request = Map.ofEntries(
        Map.entry("title", "Evento del propietario"),
        Map.entry("priority", "Media"),
        Map.entry("eventDate", start.toLocalDate()),
        Map.entry("frequency", 0),
        Map.entry("reminder", start.minusHours(1)),
        Map.entry("startHour", start),
        Map.entry("endHour", start.plusHours(1)),
        Map.entry("description", "Reunión de prueba"),
        Map.entry("status", "Pendiente"),
        Map.entry("categoryId", AGENDA_CATEGORY_ID));

    MvcResult result = mockMvc.perform(authorized(post("/api/agenda/events"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(request)))
        .andExpect(status().isCreated())
        .andReturn();
    return responseId(result);
  }

  private UUID createCredentialAsOwner() throws Exception {
    Map<String, Object> request = Map.of(
        "applicationName", "Credencial del propietario",
        "password", "secreto-temporal",
        "lastChangeDate", LocalDate.of(2026, 7, 31));

    MvcResult result = mockMvc.perform(authorized(post("/api/security/passwords"), ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(request)))
        .andExpect(status().isCreated())
        .andReturn();
    return responseId(result);
  }

  private void verifyMasterKey(String token) throws Exception {
    mockMvc.perform(authorized(post("/api/security/passwords/master-key/verify"), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("masterKey", MASTER_KEY))))
        .andExpect(status().isOk());
  }

  private AccountEntity persistAccount(String email, String masterKeyHash) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("HTTP");
    account.setAccountEmail(email);
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-login-no-utilizado");
    account.setAccountMasterKey(masterKeyHash);
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
