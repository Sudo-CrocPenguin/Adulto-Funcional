package org.adultofuncional.main.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Protege la frontera HTTP de cuentas contra enumeración y mutación ajena.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountOwnershipHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  JwtService jwtService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  AccountEntity owner;
  AccountEntity foreignAccount;
  String ownerToken;

  @BeforeEach
  void setUp() {
    owner = persistAccount("Propietario");
    foreignAccount = persistAccount("Cuenta ajena");
    ownerToken = tokenFor(owner);
  }

  @Test
  void makesExistingForeignAndUnknownAccountIndistinguishable() throws Exception {
    MvcResult foreignResult = mockMvc.perform(get("/api/account/{id}", foreignAccount.getAccountId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
        .andReturn();

    MvcResult unknownResult = mockMvc.perform(get("/api/account/{id}", UUID.randomUUID())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
        .andReturn();

    assertThat(message(foreignResult)).isEqualTo(message(unknownResult));
  }

  @Test
  void rejectsForeignUpdateAndDeleteWithoutChangingAccount() throws Exception {
    mockMvc.perform(patch("/api/account/{id}", foreignAccount.getAccountId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"names\":\"Nombre manipulado\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

    mockMvc.perform(delete("/api/account/{id}", foreignAccount.getAccountId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

    AccountEntity preserved = accountRepository.findById(foreignAccount.getAccountId()).orElseThrow();
    assertThat(preserved.getAccountNames()).isEqualTo("Cuenta ajena");
  }

  @Test
  void allowsOwnerToReadOwnAccount() throws Exception {
    mockMvc.perform(get("/api/account/{id}", owner.getAccountId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(owner.getAccountId().toString()));
  }

  private AccountEntity persistAccount(String names) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames(names);
    account.setAccountLastNames("Prueba ownership");
    account.setAccountEmail("ownership-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-login-no-utilizado");
    return accountRepository.saveAndFlush(account);
  }

  private String tokenFor(AccountEntity account) {
    return jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  private String message(MvcResult result) throws Exception {
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return body.path("message").asText();
  }
}
