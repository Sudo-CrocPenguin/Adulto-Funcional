package org.adultofuncional.main.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.adultofuncional.main.security.infrastructure.persistence.repository.PasswordJpaRepository;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
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

/** Recorre el contrato canónico y verifica el recifrado entre dos sesiones. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MasterKeyLifecycleHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final String LOGIN_PASSWORD = "frase principal extensa";
  private static final String OLD_MASTER_KEY = "MasterKey-Original-2026";
  private static final String NEW_MASTER_KEY = "MasterKey-Rotada-2026";

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Autowired
  PasswordJpaRepository passwordRepository;

  @Autowired
  MasterKeySessionService sessionService;

  private UUID accountId;

  @AfterEach
  void clearUnlocks() {
    if (accountId != null) {
      sessionService.clearAll(accountId);
    }
  }

  @Test
  void configuresRotatesReciphersAndClosesOnlyCurrentSession() throws Exception {
    AccountEntity account = persistAccount();
    accountId = account.getAccountId();
    String firstToken = tokenFor(account);
    String secondToken = tokenFor(account);

    mockMvc.perform(authorized(get("/api/security/master-key/status"), firstToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.configured").value(false))
        .andExpect(jsonPath("$.data.verified").value(false))
        .andExpect(jsonPath("$.data.expiresAt").doesNotExist());

    mockMvc.perform(authorized(post("/api/security/master-key"), firstToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "currentPassword", "contraseña incorrecta",
                "newMasterKey", OLD_MASTER_KEY))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("REAUTHENTICATION_FAILED"));

    mockMvc.perform(authorized(post("/api/security/master-key"), firstToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "currentPassword", LOGIN_PASSWORD,
                "newMasterKey", OLD_MASTER_KEY))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.configured").value(true))
        .andExpect(jsonPath("$.data.verified").value(false));

    mockMvc.perform(authorized(post("/api/security/master-key"), firstToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "currentPassword", LOGIN_PASSWORD,
                "newMasterKey", OLD_MASTER_KEY))))
        .andExpect(status().isConflict());

    verify(firstToken, OLD_MASTER_KEY);
    verify(secondToken, OLD_MASTER_KEY);
    UUID credentialId = createCredential(firstToken);

    mockMvc.perform(authorized(patch("/api/security/master-key"), firstToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "currentPassword", LOGIN_PASSWORD,
                "currentMasterKey", OLD_MASTER_KEY,
                "newMasterKey", NEW_MASTER_KEY))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.configured").value(true))
        .andExpect(jsonPath("$.data.verified").value(false));

    expectLocked(firstToken);
    expectLocked(secondToken);
    mockMvc.perform(authorized(post("/api/security/master-key/verify"), firstToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("masterKey", OLD_MASTER_KEY))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MASTER_KEY_INVALID"));

    verify(firstToken, NEW_MASTER_KEY);
    mockMvc.perform(authorized(get("/api/security/passwords/{id}", credentialId), firstToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.password").value("secreto Unicode 🔐"));
    assertThat(passwordRepository.findById(credentialId).orElseThrow()
        .getPasswordCryptoVersion()).isEqualTo((short) 2);

    verify(secondToken, NEW_MASTER_KEY);
    mockMvc.perform(authorized(delete("/api/security/master-key/session"), firstToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.verified").value(false));
    expectLocked(firstToken);
    mockMvc.perform(authorized(get("/api/security/master-key/status"), secondToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.verified").value(true))
        .andExpect(jsonPath("$.data.expiresAt").isString());
  }

  private AccountEntity persistAccount() {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Contrato");
    account.setAccountLastNames("Canónico");
    account.setAccountEmail("master-lifecycle-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("+573001234567");
    account.setAccountPassword(passwordEncoder.encode(LOGIN_PASSWORD));
    account.setAccountMasterKey(null);
    return accountRepository.saveAndFlush(account);
  }

  private UUID createCredential(String token) throws Exception {
    MvcResult result = mockMvc.perform(authorized(post("/api/security/passwords"), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of(
                "applicationName", "Servicio protegido",
                "password", "secreto Unicode 🔐",
                "lastChangeDate", LocalDate.of(2026, 8, 3)))))
        .andExpect(status().isCreated())
        .andReturn();
    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return UUID.fromString(response.at("/data/id").asText());
  }

  private void verify(String token, String masterKey) throws Exception {
    mockMvc.perform(authorized(post("/api/security/master-key/verify"), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("masterKey", masterKey))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.verified").value(true))
        .andExpect(jsonPath("$.data.expiresAt").isString());
  }

  private void expectLocked(String token) throws Exception {
    mockMvc.perform(authorized(get("/api/security/master-key/status"), token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.verified").value(false))
        .andExpect(jsonPath("$.data.expiresAt").doesNotExist());
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
}
