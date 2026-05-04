package org.adultofuncional.main.account.infrastructure.controller;

import java.util.UUID;

import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.application.usecase.GetAccountUseCase;
import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
import org.adultofuncional.main.shared.security.OwnershipValidator;
import org.springframework.http.HttpStatus;
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
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST que expone los endpoints del módulo de cuentas.
 *
 * <p>
 * Base path: {@code /api/account}. Todos los endpoints requieren autenticación
 * JWT y validan que el usuario autenticado sea el propietario de la cuenta
 * solicitada — un usuario no puede acceder ni modificar la cuenta de otro.
 * La validación de ownership se delega a {@link OwnershipValidator}.
 *
 * <p>
 * Las respuestas nunca exponen campos sensibles como {@code account_password}
 * ni {@code account_master_key}. El filtrado ocurre en la capa de aplicación
 * mediante {@link AccountResponse}.
 *
 * <p>
 * <strong>Endpoints:</strong>
 * <ul>
 * <li>{@code GET    /api/account/{id}} — obtener datos de una cuenta</li>
 * <li>{@code PATCH  /api/account/{id}} — actualizar datos de una cuenta</li>
 * <li>{@code DELETE /api/account/{id}} — eliminar cuenta y datos asociados
 * (pendiente de implementación)</li>
 * </ul>
 *
 * @author Lydis Ester Jaraba
 * @since 0.0.1
 * @see GetAccountUseCase
 * @see UpdateAccountUseCase
 * @see OwnershipValidator
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

  private final GetAccountUseCase getAccountUseCase;
  private final UpdateAccountUseCase updateAccountUseCase;
  private final OwnershipValidator ownershipValidator;

  // TODO: Inyectar DeleteAccountUseCase cuando esté implementado

  /**
   * Obtiene los datos de una cuenta por su ID.
   *
   * <p>
   * Valida ownership antes de retornar los datos. La consulta a base de datos
   * se realiza una sola vez y el resultado se reutiliza tanto para la
   * validación como para la respuesta.
   *
   * @param id          UUID de la cuenta a consultar
   * @param loggedEmail email del usuario autenticado, extraído del JWT por
   *                    {@link org.adultofuncional.main.config.security.JwtAuthenticationFilter}
   * @return 200 OK con los datos de la cuenta
   * @throws org.adultofuncional.main.shared.exception.NotFoundException
   *                                                                         si no
   *                                                                         existe
   *                                                                         una
   *                                                                         cuenta
   *                                                                         con
   *                                                                         el ID
   *                                                                         dado
   * @throws org.adultofuncional.main.shared.exception.UnauthorizedException
   *                                                                         si el
   *                                                                         usuario
   *                                                                         autenticado
   *                                                                         no es
   *                                                                         el
   *                                                                         propietario
   */
  @GetMapping("/{id}")
  public ResponseEntity<AccountResponse> getAccount(
      @PathVariable UUID id,
      @AuthenticationPrincipal String loggedEmail) {

    AccountResponse account = getAccountUseCase.execute(id);
    ownershipValidator.validate(account, loggedEmail);
    return ResponseEntity.ok(account);
  }

  /**
   * Actualiza los datos de una cuenta.
   *
   * <p>
   * Valida ownership consultando la cuenta actual antes de aplicar los cambios.
   * Si el email nuevo ya pertenece a otra cuenta, el caso de uso lanza una
   * excepción de conflicto (409).
   *
   * @param id          UUID de la cuenta a actualizar
   * @param request     nuevos datos de la cuenta, validados con Jakarta
   *                    Validation
   * @param loggedEmail email del usuario autenticado, extraído del JWT
   * @return 200 OK con los datos actualizados
   * @throws org.adultofuncional.main.shared.exception.NotFoundException
   *                                                                         si no
   *                                                                         existe
   *                                                                         una
   *                                                                         cuenta
   *                                                                         con
   *                                                                         el ID
   *                                                                         dado
   * @throws org.adultofuncional.main.shared.exception.UnauthorizedException
   *                                                                         si el
   *                                                                         usuario
   *                                                                         autenticado
   *                                                                         no es
   *                                                                         el
   *                                                                         propietario
   * @throws org.adultofuncional.main.shared.exception.ConflictException
   *                                                                         si el
   *                                                                         nuevo
   *                                                                         email
   *                                                                         ya
   *                                                                         está
   *                                                                         registrado
   *                                                                         por
   *                                                                         otra
   *                                                                         cuenta
   */
  @PatchMapping("/{id}")
  public ResponseEntity<AccountResponse> updateAccount(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateAccountRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    AccountResponse account = getAccountUseCase.execute(id);
    ownershipValidator.validate(account, loggedEmail);
    return ResponseEntity.ok(updateAccountUseCase.execute(id, request));
  }

  /**
   * Elimina una cuenta y todos sus datos asociados en cascada.
   *
   * <p>
   * Pendiente de implementación — retorna {@code 501 Not Implemented} hasta
   * que {@code DeleteAccountUseCase} esté disponible. La validación de
   * ownership se ejecuta igualmente para no exponer la existencia de una
   * cuenta a usuarios no autorizados.
   *
   * <p>
   * TODO: Conectar con {@code DeleteAccountUseCase} cuando esté implementado
   * y cambiar el status de retorno a {@code 204 No Content}.
   *
   * @param id          UUID de la cuenta a eliminar
   * @param loggedEmail email del usuario autenticado, extraído del JWT
   * @return 501 Not Implemented hasta que el caso de uso esté disponible
   * @throws org.adultofuncional.main.shared.exception.NotFoundException
   *                                                                         si no
   *                                                                         existe
   *                                                                         una
   *                                                                         cuenta
   *                                                                         con
   *                                                                         el ID
   *                                                                         dado
   * @throws org.adultofuncional.main.shared.exception.UnauthorizedException
   *                                                                         si el
   *                                                                         usuario
   *                                                                         autenticado
   *                                                                         no es
   *                                                                         el
   *                                                                         propietario
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAccount(
      @PathVariable UUID id,
      @AuthenticationPrincipal String loggedEmail) {

    AccountResponse account = getAccountUseCase.execute(id);
    ownershipValidator.validate(account, loggedEmail);
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
