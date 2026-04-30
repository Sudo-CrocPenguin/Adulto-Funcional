package org.adultofuncional.main.account.infrastructure.controller;

import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST que expone los endpoints del módulo de cuentas.
 *
 * <p>
 * Base path: {@code /api/account}. Las respuestas nunca incluyen
 * {@code account_password} ni {@code account_master_key}.
 *
 * <p>
 * Endpoints:
 * <ul>
 * <li>{@code GET    /api/account/{id}} — obtener datos de una cuenta</li>
 * <li>{@code PATCH  /api/account/{id}} — actualizar datos de una cuenta</li>
 * <li>{@code DELETE /api/account/{id}} — eliminar una cuenta</li>
 * </ul>
 *
 * @author Lydis Jaraba
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

  private final GetAccountUseCase getAccountUseCase;
  private final UpdateAccountUseCase updateAccountUseCase;

  // TODO: Agregar DeleteAccountUseCase cuando esté implementado

  public AccountController(GetAccountUseCase getAccountUseCase,
      UpdateAccountUseCase updateAccountUseCase) {
    this.getAccountUseCase = getAccountUseCase;
    this.updateAccountUseCase = updateAccountUseCase;
  }

  /**
   * Obtiene los datos de una cuenta por su ID.
   *
   * @param id UUID de la cuenta a consultar
   * @return datos de la cuenta con status 200
   */
  @GetMapping("/{id}")
  public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
    return ResponseEntity.ok(getAccountUseCase.execute(id));
  }

  /**
   * Actualiza los datos de una cuenta.
   *
   * @param id      UUID de la cuenta a actualizar
   * @param request nuevos datos validados de la cuenta
   * @return datos actualizados con status 200
   */
  @PatchMapping("/{id}")
  public ResponseEntity<AccountResponse> updateAccount(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateAccountRequest request) {
    return ResponseEntity.ok(updateAccountUseCase.execute(id, request));
  }

  /**
   * Elimina una cuenta por su ID.
   * Todos los datos asociados se eliminan en cascada.
   *
   * @param id UUID de la cuenta a eliminar
   * @return respuesta vacía con status 204
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
    // TODO: conectar con DeleteAccountUseCase cuando esté implementado
    return ResponseEntity.noContent().build();
  }
}
