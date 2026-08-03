package org.adultofuncional.main.finances;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * Verifica paginación sin perder el ownership SYSTEM/PERSONAL de categorías.
 *
 * <p><strong>Qué es:</strong> una prueba integral del catálogo visible.</p>
 *
 * <p><strong>Para qué sirve:</strong> garantiza que búsqueda, orden, límites y
 * conteos excluyan categorías personales de otras cuentas.</p>
 *
 * <p><strong>Cómo funciona:</strong> crea categorías coincidentes para dos
 * identidades y consulta únicamente el catálogo del propietario.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryPaginationHttpIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  JwtService jwtService;

  @Autowired
  SpringAccountJpaRepository accountRepository;

  String ownerToken;
  String foreignToken;

  @BeforeEach
  void setUpData() throws Exception {
    ownerToken = tokenFor(persistAccount("category-page-owner"));
    foreignToken = tokenFor(persistAccount("category-page-foreign"));
    createCategory(ownerToken, "Paginada Alfa");
    createCategory(ownerToken, "Paginada Beta");
    createCategory(ownerToken, "Paginada Gamma");
    createCategory(foreignToken, "Paginada Ajena");
  }

  @Test
  void pagesOnlyTheCatalogAccessibleToTheAccount() throws Exception {
    mockMvc.perform(authorized(get("/api/finances/categories")
            .queryParam("type", "FINANCES")
            .queryParam("searchTerm", "PAGINADA")
            .queryParam("page", "0")
            .queryParam("size", "2")
            .queryParam("sortBy", "name")
            .queryParam("sortDirection", "DESC"), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].name").value("Paginada Gamma"))
        .andExpect(jsonPath("$.data[1].name").value("Paginada Beta"))
        .andExpect(jsonPath("$.page.totalElements").value(3))
        .andExpect(jsonPath("$.page.totalPages").value(2))
        .andExpect(jsonPath("$.page.hasNext").value(true));

    mockMvc.perform(authorized(get("/api/finances/categories")
            .queryParam("searchTerm", "Paginada")
            .queryParam("page", "1")
            .queryParam("size", "2")
            .queryParam("sortBy", "name")
            .queryParam("sortDirection", "DESC"), ownerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].name").value("Paginada Alfa"))
        .andExpect(jsonPath("$.page.hasPrevious").value(true));
  }

  @Test
  void rejectsInvalidPageAndSortParameters() throws Exception {
    expectInvalid(get("/api/finances/categories").queryParam("page", "-1"));
    expectInvalid(get("/api/finances/categories").queryParam("size", "101"));
    expectInvalid(get("/api/finances/categories").queryParam("sortBy", "owner"));
    expectInvalid(get("/api/finances/categories").queryParam("sortDirection", "RANDOM"));
  }

  private void createCategory(String token, String name) throws Exception {
    mockMvc.perform(authorized(post("/api/finances/categories"), token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(Map.of(
                "name", name,
                "type", "FINANCES"))))
        .andExpect(status().isCreated());
  }

  private AccountEntity persistAccount(String prefix) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Categorías");
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
