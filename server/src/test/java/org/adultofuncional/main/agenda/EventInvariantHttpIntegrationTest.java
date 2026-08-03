package org.adultofuncional.main.agenda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Comprueba las reglas cronológicas y catálogos de agenda en el contrato HTTP.
 *
 * <p><strong>Qué es:</strong> una prueba de integración que atraviesa
 * validación MVC, aplicación, dominio, JPA y MariaDB.</p>
 *
 * <p><strong>Para qué sirve:</strong> evita que creación y PATCH acepten
 * frecuencias, prioridades, estados o combinaciones temporales inválidas.</p>
 *
 * <p><strong>Cómo funciona:</strong> crea una cuenta autenticada y un evento
 * válido, luego modifica una regla por petición y exige un 400 uniforme.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventInvariantHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  private static final UUID AGENDA_CATEGORY =
      UUID.fromString("01988e6b-0c00-7000-8000-000000000009");

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  String token;
  LocalDate eventDate;
  LocalDateTime start;

  @BeforeEach
  void setUp() {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Agenda");
    account.setAccountEmail("agenda-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    account = accountRepository.saveAndFlush(account);
    token = jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
    eventDate = LocalDate.now().plusDays(1);
    start = eventDate.atTime(10, 0);
  }

  @Test
  void acceptsAValidEventAndAConsistentPartialUpdate() throws Exception {
    UUID eventId = createValidEvent();

    mockMvc.perform(authorized(patch("/api/agenda/events/{id}", eventId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("frequency", 7, "priority", "Alta"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.frequency").value(7))
        .andExpect(jsonPath("$.data.priority").value("Alta"));
  }

  @Test
  void rejectsInvalidSchedulesDuringCreation() throws Exception {
    Map<String, Object> invalidFrequency = validRequest();
    invalidFrequency.put("frequency", 2);
    expectBusinessRule(post("/api/agenda/events"), invalidFrequency);

    Map<String, Object> lateReminder = validRequest();
    lateReminder.put("reminder", start);
    expectBusinessRule(post("/api/agenda/events"), lateReminder);

    Map<String, Object> zeroDuration = validRequest();
    zeroDuration.put("endHour", start);
    expectBusinessRule(post("/api/agenda/events"), zeroDuration);

    Map<String, Object> inconsistentDate = validRequest();
    inconsistentDate.put("eventDate", eventDate.plusDays(1));
    expectBusinessRule(post("/api/agenda/events"), inconsistentDate);
  }

  @Test
  void rejectsInvalidPartialUpdatesAndStoredHtml() throws Exception {
    UUID eventId = createValidEvent();

    expectBusinessRule(
        patch("/api/agenda/events/{id}", eventId),
        Map.of("frequency", 2));
    expectBusinessRule(
        patch("/api/agenda/events/{id}", eventId),
        Map.of("reminder", start));
    expectBusinessRule(
        patch("/api/agenda/events/{id}", eventId),
        Map.of("eventDate", eventDate.plusDays(1)));

    expectValidationFailure(
        patch("/api/agenda/events/{id}", eventId),
        Map.of("priority", "Urgente"));
    expectValidationFailure(
        patch("/api/agenda/events/{id}", eventId),
        Map.of("status", "<script>alert(1)</script>"));
  }

  private UUID createValidEvent() throws Exception {
    MvcResult result = mockMvc.perform(authorized(post("/api/agenda/events"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(validRequest())))
        .andExpect(status().isCreated())
        .andReturn();
    return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsByteArray())
        .path("data")
        .path("id")
        .asText());
  }

  private Map<String, Object> validRequest() {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("title", "Reunión de seguimiento");
    request.put("priority", "Media");
    request.put("eventDate", eventDate);
    request.put("frequency", 0);
    request.put("reminder", start.minusHours(1));
    request.put("startHour", start);
    request.put("endHour", start.plusHours(1));
    request.put("description", "Revisar avances");
    request.put("status", "Pendiente");
    request.put("categoryId", AGENDA_CATEGORY);
    return request;
  }

  private void expectBusinessRule(
      MockHttpServletRequestBuilder request,
      Object body) throws Exception {
    mockMvc.perform(authorized(request)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
  }

  private void expectValidationFailure(
      MockHttpServletRequestBuilder request,
      Object body) throws Exception {
    mockMvc.perform(authorized(request)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }

  private byte[] json(Object value) throws Exception {
    return objectMapper.writeValueAsBytes(value);
  }
}
