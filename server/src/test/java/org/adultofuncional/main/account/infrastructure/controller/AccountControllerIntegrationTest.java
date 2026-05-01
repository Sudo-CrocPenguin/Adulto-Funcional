package org.adultofuncional.main.account.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para {@link AccountController} usando {@link WebMvcTest}.
 *
 * <p>
 * Este enfoque carga solo la capa web (controladores, validación, conversión de mensajes)
 * y simula los casos de uso con Mockito. Es más rápido que {@code @SpringBootTest}
 * porque no carga la configuración completa de Spring ni la base de datos.
 *
 * <p>
 * Endpoints probados:
 * <ul>
 *   <li>{@code GET /api/account/{id}} — obtener cuenta</li>
 *   <li>{@code PATCH /api/account/{id}} — actualizar cuenta</li>
 *   <li>{@code DELETE /api/account/{id}} — eliminar cuenta</li>
 * </ul>
 *
 * @since 0.0.1
 */
@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
class AccountControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private GetAccountUseCase getAccountUseCase;

  @MockitoBean
  private UpdateAccountUseCase updateAccountUseCase;

  @Test
  @DisplayName("GET /api/account/{id} debe retornar 200 con los datos de la cuenta cuando existe")
  void getAccount_shouldReturn200WhenAccountExists() throws Exception {
    UUID accountId = UUID.randomUUID();
    AccountResponse response = AccountResponse.builder()
        .id(accountId)
        .names("Juan")
        .lastnames("Perez")
        .email("juan@example.com")
        .phone("+573001234567")
        .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
        .build();

    given(getAccountUseCase.execute(accountId)).willReturn(response);

    mockMvc.perform(get("/api/account/{id}", accountId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(accountId.toString()))
        .andExpect(jsonPath("$.names").value("Juan"))
        .andExpect(jsonPath("$.lastnames").value("Perez"))
        .andExpect(jsonPath("$.email").value("juan@example.com"))
        .andExpect(jsonPath("$.phone").value("+573001234567"))
        .andExpect(jsonPath("$.createdAt").value("2024-01-15T10:30:00"));
  }

  @Test
  @DisplayName("GET /api/account/{id} debe retornar 404 cuando la cuenta no existe")
  void getAccount_shouldReturn404WhenAccountNotFound() throws Exception {
    UUID accountId = UUID.randomUUID();

    given(getAccountUseCase.execute(accountId))
        .willThrow(new NotFoundException("Cuenta no encontrada con id: " + accountId));

    mockMvc.perform(get("/api/account/{id}", accountId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Cuenta no encontrada con id: " + accountId))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  @DisplayName("GET /api/account/{id} debe retornar 500 cuando el ID tiene formato inválido")
  void getAccount_shouldReturn500WhenIdIsInvalid() throws Exception {
    mockMvc.perform(get("/api/account/{id}", "invalid-uuid")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe retornar 200 con los datos actualizados")
  void updateAccount_shouldReturn200WithUpdatedData() throws Exception {
    UUID accountId = UUID.randomUUID();
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("Maria Fernanda")
        .lastnames("Gomez Lopez")
        .phone("+573101112233")
        .email("mariafernanda@example.com")
        .build();

    AccountResponse response = AccountResponse.builder()
        .id(accountId)
        .names("Maria Fernanda")
        .lastnames("Gomez Lopez")
        .email("mariafernanda@example.com")
        .phone("+573101112233")
        .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
        .build();

    given(updateAccountUseCase.execute(eq(accountId), any(UpdateAccountRequest.class)))
        .willReturn(response);

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(accountId.toString()))
        .andExpect(jsonPath("$.names").value("Maria Fernanda"))
        .andExpect(jsonPath("$.lastnames").value("Gomez Lopez"))
        .andExpect(jsonPath("$.email").value("mariafernanda@example.com"))
        .andExpect(jsonPath("$.phone").value("+573101112233"));
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe retornar 404 cuando la cuenta no existe")
  void updateAccount_shouldReturn404WhenAccountNotFound() throws Exception {
    UUID accountId = UUID.randomUUID();
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("Test")
        .lastnames("User")
        .phone("+573001112233")
        .email("test@example.com")
        .build();

    given(updateAccountUseCase.execute(eq(accountId), any(UpdateAccountRequest.class)))
        .willThrow(new NotFoundException("Cuenta no encontrada con id: " + accountId));

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Cuenta no encontrada con id: " + accountId));
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe retornar 400 cuando el email ya está registrado")
  void updateAccount_shouldReturn400WhenEmailAlreadyExists() throws Exception {
    UUID accountId = UUID.randomUUID();
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("User2 Updated")
        .lastnames("Two Updated")
        .phone("+573003333333")
        .email("existing@example.com")
        .build();

    given(updateAccountUseCase.execute(eq(accountId), any(UpdateAccountRequest.class)))
        .willThrow(new BusinessException("El email existing@example.com ya está registrado por otra cuenta"));

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("El email existing@example.com ya está registrado por otra cuenta"));
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe retornar 400 cuando el request tiene campos vacíos")
  void updateAccount_shouldReturn400WhenRequestHasBlankFields() throws Exception {
    UUID accountId = UUID.randomUUID();
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("")
        .lastnames("ValidLastName")
        .phone("+573001234567")
        .email("valid@example.com")
        .build();

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Error de validación"))
        .andExpect(jsonPath("$.data.names").value("El nombre es obligatorio"));
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe retornar 400 cuando el email tiene formato inválido")
  void updateAccount_shouldReturn400WhenEmailIsInvalid() throws Exception {
    UUID accountId = UUID.randomUUID();
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("Test")
        .lastnames("User")
        .phone("+573001234567")
        .email("not-an-email")
        .build();

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.email").value("Debe ser un email válido"));
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe retornar 400 cuando el nombre excede 50 caracteres")
  void updateAccount_shouldReturn400WhenNameExceedsMaxLength() throws Exception {
    UUID accountId = UUID.randomUUID();
    String longName = "A".repeat(51);

    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names(longName)
        .lastnames("ValidLastName")
        .phone("+573001234567")
        .email("valid@example.com")
        .build();

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.names").value("El nombre no puede exceder 50 caracteres"));
  }

  @Test
  @DisplayName("DELETE /api/account/{id} debe retornar 204 al eliminar una cuenta")
  void deleteAccount_shouldReturn204() throws Exception {
    UUID accountId = UUID.randomUUID();

    mockMvc.perform(delete("/api/account/{id}", accountId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("GET /api/account/{id} no debe exponer password ni masterKey en la respuesta")
  void getAccount_shouldNotExposeSensitiveFields() throws Exception {
    UUID accountId = UUID.randomUUID();
    AccountResponse response = AccountResponse.builder()
        .id(accountId)
        .names("Secure")
        .lastnames("User")
        .email("secure@example.com")
        .phone("+573007776666")
        .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
        .build();

    given(getAccountUseCase.execute(accountId)).willReturn(response);

    mockMvc.perform(get("/api/account/{id}", accountId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.masterKey").doesNotExist())
        .andExpect(jsonPath("$.account_password").doesNotExist())
        .andExpect(jsonPath("$.account_master_key").doesNotExist());
  }

  @Test
  @DisplayName("GET /api/account/{id} debe llamar al use case con el ID correcto")
  void getAccount_shouldCallUseCaseWithCorrectId() throws Exception {
    UUID accountId = UUID.randomUUID();
    AccountResponse response = AccountResponse.builder()
        .id(accountId)
        .names("Test")
        .lastnames("User")
        .email("test@example.com")
        .phone("+573001234567")
        .createdAt(LocalDateTime.now())
        .build();

    given(getAccountUseCase.execute(accountId)).willReturn(response);

    mockMvc.perform(get("/api/account/{id}", accountId))
        .andExpect(status().isOk());

    verify(getAccountUseCase).execute(accountId);
  }

  @Test
  @DisplayName("PATCH /api/account/{id} debe llamar al use case con el ID y request correctos")
  void updateAccount_shouldCallUseCaseWithCorrectArguments() throws Exception {
    UUID accountId = UUID.randomUUID();
    UpdateAccountRequest request = UpdateAccountRequest.builder()
        .names("Test")
        .lastnames("User")
        .phone("+573001234567")
        .email("test@example.com")
        .build();

    AccountResponse response = AccountResponse.builder()
        .id(accountId)
        .names("Test")
        .lastnames("User")
        .email("test@example.com")
        .phone("+573001234567")
        .createdAt(LocalDateTime.now())
        .build();

    given(updateAccountUseCase.execute(eq(accountId), any(UpdateAccountRequest.class)))
        .willReturn(response);

    mockMvc.perform(patch("/api/account/{id}", accountId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(updateAccountUseCase).execute(eq(accountId), any(UpdateAccountRequest.class));
  }
}
