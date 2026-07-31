package org.adultofuncional.main.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

class JwtAuthenticationFilterTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void usesJwtSubjectAsStableAuthenticatedAccountId() throws Exception {
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setSecret("test-jwt-secret-with-at-least-32-characters");
    jwtProperties.setExpiration(60_000);

    JwtService jwtService = new JwtService(jwtProperties);
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, new ObjectMapper());
    UUID accountId = UUID.randomUUID();

    String token = jwtService.generateToken(
        accountId.toString(),
        "correo-antiguo@example.com",
        List.of(new SimpleGrantedAuthority("ROLE_USER")));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, new MockFilterChain());

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    assertThat(principal).isInstanceOf(AuthenticatedAccount.class);
    AuthenticatedAccount authenticatedAccount = (AuthenticatedAccount) principal;
    assertThat(authenticatedAccount.accountId()).isEqualTo(accountId);
    assertThat(authenticatedAccount.email()).isEqualTo("correo-antiguo@example.com");
  }

  @Test
  void allowsPublicAuthEndpointsWhenCookieTokenIsInvalid() throws Exception {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService(), new ObjectMapper());

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
    request.setContentType("application/json");
    request.setCookies(new Cookie("token", "token-invalido"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void rejectsProtectedEndpointsWhenCookieTokenIsInvalid() throws Exception {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService(), new ObjectMapper());

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/finances/movements");
    request.setCookies(new Cookie("token", "token-invalido"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, new MockFilterChain());

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    assertThat(response.getContentType()).contains("charset=UTF-8");
    assertThat(response.getContentAsString()).contains("Token JWT inválido");
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  private JwtService jwtService() {
    JwtProperties jwtProperties = new JwtProperties();
    jwtProperties.setSecret("test-jwt-secret-with-at-least-32-characters");
    jwtProperties.setExpiration(60_000);
    return new JwtService(jwtProperties);
  }
}
