package org.adultofuncional.main.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifica el listado paginado y no sensible de la bóveda.
 *
 * <p><strong>Qué es:</strong> una prueba integral con sesión JWT, Master Key,
 * cifrado real y MariaDB.</p>
 *
 * <p><strong>Para qué sirve:</strong> acota el consumo del listado sin mezclar
 * sesiones ni exponer credenciales de otra cuenta.</p>
 *
 * <p><strong>Cómo funciona:</strong> desbloquea dos sesiones, crea entradas
 * coincidentes y valida búsqueda, orden, metadatos y parámetros negativos.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PasswordPaginationHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final String MASTER_KEY = "Master-Key-Paginacion-2026";

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Autowired
  MasterKeySessionService masterKeySessionService;

  AccountEntity owner;
  AccountEntity foreignAccount;
  String ownerToken;
  String foreignToken;

  @BeforeEach
  void setUpData() throws Exception {
    owner = persistAccount("password-page-owner");
    foreignAccount = persistAccount("password-page-foreign");
    ownerToken = tokenFor(owner);
    foreignToken = tokenFor(foreignAccount);
    unlock(owner, ownerToken);
    unlock(foreignAccount, foreignToken);

    createCredential(ownerToken, "Paginada Alfa", 3);
    createCredential(ownerToken, "Paginada Beta", 2);
    createCredential(ownerToken, "Paginada Gamma", 1);
    createCredential(foreignToken, "Paginada Ajena", 1);
  }

  @AfterEach
  void clearSessions() {
    masterKeySessionService.clearAll(owner.getAccountId());
    masterKeySessionService.clearAll(foreignAccount.getAccountId());
  }

  @Test
  void pagesOnlyNonSensitiveCredentialsFromTheCurrentAccount() throws Exception {
    mockMvc.perform(authorized(get("/api/security/passwords")
            .queryParam("searchTerm", "PAGINADA")
            .queryParam("page", "0")
            .queryParam("size", "2")
            .queryParam("sortBy", "applicationName")
            .queryParam("sortDirection", "DESC"), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].applicationName").value("Paginada Gamma"))
        .andExpect(jsonPath("$.data[1].applicationName").value("Paginada Beta"))
        .andExpect(jsonPath("$.data[0].password").doesNotExist())
        .andExpect(jsonPath("$.page.totalElements").value(3))
        .andExpect(jsonPath("$.page.totalPages").value(2))
        .andExpect(jsonPath("$.page.hasNext").value(true));
  }

  @Test
  void rejectsInvalidPageAndSortParameters() throws Exception {
    expectInvalid(get("/api/security/passwords").queryParam("page", "-1"));
    expectInvalid(get("/api/security/passwords").queryParam("size", "101"));
    expectInvalid(get("/api/security/passwords").queryParam("sortBy", "password"));
    expectInvalid(get("/api/security/passwords").queryParam("sortDirection", "RANDOM"));
  }

  private void createCredential(String token, String applicationName, long changedDaysAgo)
      throws Exception {
    Map<String, Object> request = Map.of(
        "applicationName", applicationName,
        "password", "secreto-" + applicationName,
        "lastChangeDate", LocalDate.now().minusDays(changedDaysAgo));
    mockMvc.perform(authorized(post("/api/security/passwords"), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request)))
        .andExpect(status().isCreated());
  }

  private AccountEntity persistAccount(String prefix) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Bóveda");
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

  private void unlock(AccountEntity account, String token) {
    masterKeySessionService.unlock(
        account.getAccountId(),
        jwtService.extractSessionId(token),
        MASTER_KEY);
  }

  private void expectInvalid(MockHttpServletRequestBuilder request) throws Exception {
    mockMvc.perform(authorized(request, ownerToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETER_INVALID"));
  }

  private MockHttpServletRequestBuilder authorized(
      MockHttpServletRequestBuilder request,
      String token) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }
}
