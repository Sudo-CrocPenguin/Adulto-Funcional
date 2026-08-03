package org.adultofuncional.main;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.shared.observability.TraceIdProvider;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pruebas de contrato para errores que atraviesan la cadena HTTP completa.
 *
 * <p>Verifica que seguridad y Spring MVC compartan estado, código, campos
 * históricos, trazabilidad y tipo de contenido. Se usa un JWT firmado por el
 * servicio real para comprobar tanto autenticación como autorización.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityErrorContractIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  JwtService jwtService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Test
  void returnsUniformContractWhenAuthenticationIsMissing() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/finances/categories"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
        .andExpect(jsonPath("$.message").isString())
        .andExpect(jsonPath("$.fieldErrors").isEmpty())
        .andExpect(jsonPath("$.traceId").isString())
        .andExpect(jsonPath("$.data").doesNotExist())
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  @ExtendWith(OutputCaptureExtension.class)
  void returnsSpecificCodeForInvalidJwt(CapturedOutput output) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/finances/categories")
            .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
        .andExpect(jsonPath("$.code").value("JWT_INVALID"))
        .andExpect(jsonPath("$.fieldErrors").isEmpty())
        .andReturn();

    assertUniformTransport(result);
    assertThat(output.getOut())
        .contains("traceId=" + result.getResponse().getHeader(TraceIdProvider.TRACE_ID_HEADER));
  }

  @Test
  void returnsBearerChallengeWhenCredentialsAreInvalid() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "cuenta-inexistente@example.com",
                  "password": "password-seguro"
                }
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
        .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  void returnsUniformForbiddenContractForLockedVault() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/security/passwords")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + persistedUserToken()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("MASTER_KEY_REQUIRED"))
        .andExpect(jsonPath("$.data").doesNotExist())
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  void returnsUniformContractWhenCorsRejectsActualRequest() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/finances/categories")
            .header(HttpHeaders.ORIGIN, "https://origen-no-permitido.example"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("CORS_REQUEST_REJECTED"))
        .andExpect(jsonPath("$.fieldErrors").isEmpty())
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  void returnsUniformContractWhenCorsRejectsPreflightRequest() throws Exception {
    MvcResult result = mockMvc.perform(options("/api/finances/categories")
            .header(HttpHeaders.ORIGIN, "https://origen-no-permitido.example")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("CORS_REQUEST_REJECTED"))
        .andExpect(jsonPath("$.fieldErrors").isEmpty())
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  void mapsUnknownAuthenticatedRouteToEndpointNotFound() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/ruta-inexistente")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"))
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  void returnsOrderedFieldErrorsForInvalidRequest() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("email"))
        .andExpect(jsonPath("$.fieldErrors[0].code").value("NotBlank"))
        .andExpect(jsonPath("$.fieldErrors[1].field").value("password"))
        .andExpect(jsonPath("$.fieldErrors[1].code").value("NotBlank"))
        .andReturn();

    assertUniformTransport(result);
  }

  @Test
  void distinguishesMalformedJsonMethodAndMediaTypeErrors() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQUEST_BODY_INVALID"));

    mockMvc.perform(get("/api/auth/login"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.TEXT_PLAIN)
            .content("contenido"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.code").value("MEDIA_TYPE_UNSUPPORTED"));
  }

  private String userToken() {
    return jwtService.generateToken(
        UUID.randomUUID().toString(),
        "contrato-errores@example.com",
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  private String persistedUserToken() {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Contrato");
    account.setAccountLastNames("Errores");
    account.setAccountEmail("contrato-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    account = accountRepository.saveAndFlush(account);
    return jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  private void assertUniformTransport(MvcResult result) throws Exception {
    String contentType = result.getResponse().getContentType();
    String headerTraceId = result.getResponse().getHeader(TraceIdProvider.TRACE_ID_HEADER);
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());

    assertThat(contentType).isEqualTo("application/json;charset=UTF-8");
    assertThat(headerTraceId).isNotBlank().hasSize(32);
    assertThat(body.path("traceId").asText()).isEqualTo(headerTraceId);
    assertThat(body.has("data")).isTrue();
    assertThat(body.get("data").isNull()).isTrue();
  }
}
