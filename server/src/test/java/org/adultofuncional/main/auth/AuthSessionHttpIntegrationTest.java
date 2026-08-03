package org.adultofuncional.main.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.config.security.CookieUtils;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSessionHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private SpringAccountJpaRepository accountRepository;

  @AfterEach
  void cleanAccounts() {
    accountRepository.deleteAll();
  }

  @Test
  void nativeSessionRotatesRefreshAndRevokesEveryPreviousAccessToken() throws Exception {
    JsonNode registered = registerNative();
    UUID accountId = UUID.fromString(registered.at("/data/accountId").asText());
    String firstAccess = registered.at("/data/token").asText();
    String firstRefresh = registered.at("/data/refreshToken").asText();

    assertThat(jwtService.extractSessionId(firstAccess)).isNotNull();
    assertThat(jwtService.extractTokenId(firstAccess)).isNotNull();
    assertThat(registered.at("/data/roles").get(0).asText()).isEqualTo("ROLE_USER");

    MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
            .header("User-Agent", "okhttp/5")
            .header("X-Client-Type", "mobile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(new RefreshBody(firstRefresh))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").isString())
        .andExpect(jsonPath("$.data.refreshToken").isString())
        .andReturn();

    JsonNode refreshed = objectMapper.readTree(refreshResult.getResponse().getContentAsByteArray());
    String secondAccess = refreshed.at("/data/token").asText();
    assertThat(secondAccess).isNotEqualTo(firstAccess);
    assertThat(refreshed.at("/data/refreshToken").asText()).isNotEqualTo(firstRefresh);

    mockMvc.perform(get("/api/account/{id}", accountId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstAccess))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_SESSION_REVOKED"));

    mockMvc.perform(post("/api/auth/logout")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondAccess))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/account/{id}", accountId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondAccess))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_SESSION_REVOKED"));
  }

  @Test
  void browserKeepsBothTokensOutOfBodyAndScopesRefreshCookie() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/register")
            .header("User-Agent", "Mozilla/5.0")
            .header(HttpHeaders.ORIGIN, "http://localhost:3000")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registrationJson()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.token").doesNotExist())
        .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
        .andReturn();

    Cookie accessCookie = result.getResponse().getCookie(CookieUtils.ACCESS_TOKEN_COOKIE);
    Cookie refreshCookie = result.getResponse().getCookie(CookieUtils.REFRESH_TOKEN_COOKIE);
    assertThat(accessCookie).isNotNull();
    assertThat(accessCookie.isHttpOnly()).isTrue();
    assertThat(accessCookie.getPath()).isEqualTo("/");
    assertThat(refreshCookie).isNotNull();
    assertThat(refreshCookie.isHttpOnly()).isTrue();
    assertThat(refreshCookie.getPath()).isEqualTo(CookieUtils.REFRESH_TOKEN_PATH);
  }

  private JsonNode registerNative() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/register")
            .header("User-Agent", "okhttp/5")
            .header("X-Client-Type", "mobile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registrationJson()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.token").isString())
        .andExpect(jsonPath("$.data.refreshToken").isString())
        .andExpect(jsonPath("$.data.sessionId").isString())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsByteArray());
  }

  private String registrationJson() throws Exception {
    return objectMapper.writeValueAsString(new RegistrationBody(
        "Ángela",
        "Pérez",
        "+573001234567",
        "session-" + UUID.randomUUID() + "@example.com",
        "correct-horse-battery"));
  }

  private record RegistrationBody(
      String names,
      String lastnames,
      String phone,
      String email,
      String password) {
  }

  private record RefreshBody(String refreshToken) {
  }
}
