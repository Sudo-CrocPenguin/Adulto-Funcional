package org.adultofuncional.main.account.infrastructure.controller;

import java.util.UUID;

import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

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
 * @author Lydis Ester Jaraba
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
   * @param id          UUID de la cuenta a consultar
   * @param loggedEmail email del usuario autenticado (extraido del JWT)
   * @return datos de la cuenta con status 200
   */
  @GetMapping("/{id}")
  public ResponseEntity<AccountResponse> getAccount(
      @PathVariable UUID id,
      @AuthenticationPrincipal String loggedEmail) {

    validateOwnership(id, loggedEmail);
    return ResponseEntity.ok(getAccountUseCase.execute(id));
  }

  /**
   * Actualiza los datos de una cuenta.
   *
   * @param id          UUID de la cuenta a actualizar
   * @param request     nuevos datos validados de la cuenta
   * @param loggedEmail email del usuario autenticado (extraido del JWT)
   * @return datos actualizados con status 200
   */
  @PatchMapping("/{id}")
  public ResponseEntity<AccountResponse> updateAccount(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateAccountRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    validateOwnership(id, loggedEmail);
    return ResponseEntity.ok(updateAccountUseCase.execute(id, request));
  }

  /**
   * Elimina una cuenta por su ID.
   * Todos los datos asociados se eliminan en cascada.
   *
   * @param id          UUID de la cuenta a eliminar
   * @param loggedEmail email del usuario autenticado (extraido del JWT)
   * @return respuesta vacía con status 204
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(
      @PathVariable UUID id,
      @AuthenticationPrincipal String loggedEmail) {

    validateOwnership(id, loggedEmail);
    // TODO: conectar con DeleteAccountUseCase cuando esté implementado
    return ResponseEntity.noContent().build();
  }

 /**
   * Verifica que el usuario autenticado sea el propietario de la cuenta.
   * Consulta la cuenta por ID y compara el email con el del token JWT.
   *
   * @throws UnauthorizedException si el usuario no es el propietario
   */
  private void validateOwnership(UUID id, String loggedEmail) {
    AccountResponse account = getAccountUseCase.execute(id);
    if (!account.getEmail().equals(loggedEmail)) {
      throw new UnauthorizedException("No tienes permiso para acceder a esta cuenta");
    }
}
