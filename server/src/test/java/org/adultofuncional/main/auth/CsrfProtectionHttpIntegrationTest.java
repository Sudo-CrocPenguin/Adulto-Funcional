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
class CsrfProtectionHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final String CSRF_COOKIE = "XSRF-TOKEN";
  private static final String CSRF_HEADER = "X-XSRF-TOKEN";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private SpringAccountJpaRepository accountRepository;

  @AfterEach
  void cleanAccounts() {
    accountRepository.deleteAll();
  }

  @Test
  void rejectsCookieAuthenticatedMutationWithoutCsrfAndAcceptsDoubleSubmitToken() throws Exception {
    BrowserCredentials credentials = registerBrowser();

    mockMvc.perform(post("/api/auth/logout")
            .cookie(credentials.accessCookie()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"));

    CsrfMaterial csrf = fetchCsrf();
    mockMvc.perform(post("/api/auth/logout")
            .cookie(credentials.accessCookie(), csrf.cookie())
            .header(csrf.headerName(), csrf.token()))
        .andExpect(status().isOk());
  }

  @Test
  void protectsRefreshCookieButAllowsAValidNativeBearerWithoutCsrf() throws Exception {
    BrowserCredentials browser = registerBrowser();

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(browser.refreshCookie()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"));

    JsonNode nativeSession = registerNative();
    mockMvc.perform(post("/api/auth/logout")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + nativeSession.at("/data/token").asText()))
        .andExpect(status().isOk());
  }

  private BrowserCredentials registerBrowser() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/register")
            .header("User-Agent", "Mozilla/5.0")
            .header(HttpHeaders.ORIGIN, "http://localhost:3000")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registrationJson()))
        .andExpect(status().isCreated())
        .andReturn();
    Cookie access = result.getResponse().getCookie(CookieUtils.ACCESS_TOKEN_COOKIE);
    Cookie refresh = result.getResponse().getCookie(CookieUtils.REFRESH_TOKEN_COOKIE);
    assertThat(access).isNotNull();
    assertThat(refresh).isNotNull();
    return new BrowserCredentials(access, refresh);
  }

  private JsonNode registerNative() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/register")
            .header("User-Agent", "okhttp/5")
            .header("X-Client-Type", "mobile")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registrationJson()))
        .andExpect(status().isCreated())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsByteArray());
  }

  private CsrfMaterial fetchCsrf() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.token").isString())
        .andExpect(jsonPath("$.data.headerName").value(CSRF_HEADER))
        .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    Cookie cookie = result.getResponse().getCookie(CSRF_COOKIE);
    assertThat(cookie).isNotNull();
    return new CsrfMaterial(
        body.at("/data/token").asText(),
        body.at("/data/headerName").asText(),
        cookie);
  }

  private String registrationJson() throws Exception {
    return objectMapper.writeValueAsString(new RegistrationBody(
        "María",
        "López",
        "+573001234567",
        "csrf-" + UUID.randomUUID() + "@example.com",
        "correct-horse-battery"));
  }

  private record RegistrationBody(
      String names,
      String lastnames,
      String phone,
      String email,
      String password) {
  }

  private record BrowserCredentials(Cookie accessCookie, Cookie refreshCookie) {
  }

  private record CsrfMaterial(String token, String headerName, Cookie cookie) {
  }
}
