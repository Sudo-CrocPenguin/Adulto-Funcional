package org.adultofuncional.main.agenda;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Comprueba el listado paginado de agenda contra MariaDB.
 *
 * <p><strong>Qué es:</strong> una prueba de contrato HTTP del ADR 0005.</p>
 *
 * <p><strong>Para qué sirve:</strong> protege el rango inclusivo, filtros,
 * orden determinista, límites y metadatos sin cargar toda la agenda.</p>
 *
 * <p><strong>Cómo funciona:</strong> crea cinco eventos y consulta distintas
 * páginas y combinaciones negativas mediante un JWT real.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventPaginationHttpIntegrationTest extends MariaDbIntegrationTestSupport {

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
  LocalDate baseDate;

  @BeforeEach
  void setUpData() throws Exception {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Agenda paginada");
    account.setAccountEmail("event-page-" + UUID.randomUUID() + "@example.com");
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-no-utilizado");
    account = accountRepository.saveAndFlush(account);
    token = jwtService.generateToken(
        account.getAccountId().toString(),
        account.getAccountEmail(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
    baseDate = LocalDate.now().plusDays(1);

    createEvent(1, "Baja", "Pendiente");
    createEvent(2, "Alta", "Pendiente");
    createEvent(3, "Alta", "Completado");
    createEvent(4, "Alta", "Pendiente");
    createEvent(5, "Media", "Cancelado");
  }

  @Test
  void returnsAChronologicalPageWithMetadata() throws Exception {
    mockMvc.perform(authorized(get("/api/agenda/events")
            .queryParam("page", "0")
            .queryParam("size", "2")
            .queryParam("sortBy", "eventDate")
            .queryParam("sortDirection", "DESC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].title").value("Evento 5"))
        .andExpect(jsonPath("$.data[1].title").value("Evento 4"))
        .andExpect(jsonPath("$.page.totalElements").value(5))
        .andExpect(jsonPath("$.page.totalPages").value(3))
        .andExpect(jsonPath("$.page.hasNext").value(true));
  }

  @Test
  void combinesInclusiveDateStatusAndPriorityFilters() throws Exception {
    mockMvc.perform(authorized(get("/api/agenda/events")
            .queryParam("startDate", baseDate.plusDays(2).toString())
            .queryParam("endDate", baseDate.plusDays(4).toString())
            .queryParam("status", "Pendiente")
            .queryParam("priority", "Alta")
            .queryParam("sortBy", "eventDate")
            .queryParam("sortDirection", "ASC")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].title").value("Evento 2"))
        .andExpect(jsonPath("$.data[1].title").value("Evento 4"))
        .andExpect(jsonPath("$.page.totalElements").value(2));
  }

  @Test
  void rejectsInvalidRangesPagesAndSorts() throws Exception {
    expectInvalid(get("/api/agenda/events")
        .queryParam("startDate", baseDate.plusDays(2).toString())
        .queryParam("endDate", baseDate.toString()));
    expectInvalid(get("/api/agenda/events").queryParam("page", "-1"));
    expectInvalid(get("/api/agenda/events").queryParam("size", "101"));
    expectInvalid(get("/api/agenda/events").queryParam("sortBy", "description"));
    expectInvalid(get("/api/agenda/events").queryParam("sortDirection", "RANDOM"));
  }

  private void createEvent(int day, String priority, String status) throws Exception {
    LocalDate eventDate = baseDate.plusDays(day);
    LocalDateTime start = eventDate.atTime(9, 0);
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("title", "Evento " + day);
    request.put("priority", priority);
    request.put("eventDate", eventDate);
    request.put("frequency", 0);
    request.put("reminder", start.minusHours(1));
    request.put("startHour", start);
    request.put("endHour", start.plusHours(1));
    request.put("description", "Evento paginado");
    request.put("status", status);
    request.put("categoryId", AGENDA_CATEGORY);
    mockMvc.perform(authorized(post("/api/agenda/events"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request)))
        .andExpect(status().isCreated());
  }

  private void expectInvalid(MockHttpServletRequestBuilder request) throws Exception {
    mockMvc.perform(authorized(request))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PARAMETER_INVALID"));
  }

  private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }
}
