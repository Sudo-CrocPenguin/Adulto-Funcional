package org.adultofuncional.main.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica la semántica HTTP de Master Key definida por los ADR 0001 y 0006.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MasterKeyErrorContractIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final String MASTER_KEY = "MasterKey-Valid-2026";

  @Autowired
  MockMvc mockMvc;

  @Autowired
  JwtService jwtService;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Test
  void returnsForbiddenWithoutInvalidatingAuthenticationForWrongMasterKey() throws Exception {
    AccountEntity account = persistAccount(passwordEncoder.encode(MASTER_KEY));

    mockMvc.perform(post("/api/security/passwords/master-key/verify")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(account))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"masterKey\":\"MasterKey-Incorrecta\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.code").value("MASTER_KEY_INVALID"))
        .andExpect(jsonPath("$.fieldErrors").isEmpty())
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void returnsConflictWhenMasterKeyIsNotConfigured() throws Exception {
    AccountEntity account = persistAccount(null);

    mockMvc.perform(post("/api/security/passwords/master-key/verify")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(account))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"masterKey\":\"MasterKey-Valid-2026\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("MASTER_KEY_NOT_CONFIGURED"));
  }

  @Test
  void validatesHistoricalVerifyRequestWithTypedDto() throws Exception {
    AccountEntity account = persistAccount(passwordEncoder.encode(MASTER_KEY));

    mockMvc.perform(post("/api/security/passwords/master-key/verify")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(account))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("masterKey"))
        .andExpect(jsonPath("$.fieldErrors[0].code").value("NotBlank"));
  }

  @Test
  void identifiesLockedVaultWithMasterKeyRequiredCode() throws Exception {
    AccountEntity account = persistAccount(passwordEncoder.encode(MASTER_KEY));

    mockMvc.perform(get("/api/security/passwords")
            .header(HttpHeaders.AUTHORIZATION, bearerToken(account)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MASTER_KEY_REQUIRED"));
  }

  private AccountEntity persistAccount(String masterKeyHash) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Contrato");
    account.setAccountLastNames("Master Key");
    account.setAccountEmail("master-key-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-login-no-utilizado");
    account.setAccountMasterKey(masterKeyHash);
    return accountRepository.saveAndFlush(account);
  }

  private String bearerToken(AccountEntity account) {
    String token = jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
    return "Bearer " + token;
  }
}
