package org.adultofuncional.main.security.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.config.security.AuthenticatedAccount;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.application.dto.VerifyMasterKeyRequest;
import org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.DeletePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.GetPasswordUseCase;
import org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase;
import org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.VerifyMasterKeyUseCase;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.adultofuncional.main.shared.ratelimit.RateLimitGuard;
import org.adultofuncional.main.shared.ratelimit.RateLimitPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del módulo de gestión de contraseñas.
 *
 * <p>
 * Expone los endpoints para administrar las credenciales almacenadas:
 * crear, listar, obtener (descifrada), actualizar y eliminar, bajo la ruta
 * base {@code /api/security/passwords}. La ruta histórica de verificación se
 * conserva temporalmente y delega en el mismo caso de uso canónico.
 *
 * <p>
   * El {@code accountId} se toma del claim {@code sub} del JWT mediante
   * {@link AuthenticatedAccount}, evitando que el cliente manipule
   * identificadores de cuenta o dependa del email mutable.
 *
 * <p>
 * Todos los endpoints del gestor (excepto la verificación de Master Key)
 * exigen que la Master Key haya sido verificada previamente. Los casos de
 * uso lanzan {@link ForbiddenException} si no es así.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see CreatePasswordUseCase
 * @see ListPasswordsUseCase
 * @see GetPasswordUseCase
 * @see UpdatePasswordUseCase
 * @see DeletePasswordUseCase
 */
@RestController
@RequestMapping("/api/security/passwords")
@RequiredArgsConstructor
public class PasswordController {

  private final CreatePasswordUseCase createPasswordUseCase;
  private final ListPasswordsUseCase listPasswordsUseCase;
  private final GetPasswordUseCase getPasswordUseCase;
  private final UpdatePasswordUseCase updatePasswordUseCase;
  private final DeletePasswordUseCase deletePasswordUseCase;
  private final VerifyMasterKeyUseCase verifyMasterKeyUseCase;
  private final RateLimitGuard rateLimitGuard;

  /**
   * Retorna el identificador estable de la cuenta autenticada.
   */
  private UUID resolveAccountId(AuthenticatedAccount authenticatedAccount) {
    return authenticatedAccount.accountId();
  }

  /**
   * Verifica la Master Key del usuario autenticado y la mantiene en
   * sesión para las operaciones del gestor.
   *
   * <p>
   * Delega en el caso de uso canónico durante la ventana de compatibilidad.
   *
   * @param request     DTO validado con la Master Key en texto plano.
   * @param authenticatedAccount cuenta autenticada.
   * @return {@code 200 OK} si la Master Key es correcta.
   * @throws NotFoundException     si la cuenta no existe.
   * @throws ConflictException  si la Master Key no está configurada.
   * @throws ForbiddenException si la Master Key es incorrecta.
   */
  @PostMapping("/master-key/verify")
  public ResponseEntity<ApiResponse<Void>> verifyMasterKey(
      @Valid @RequestBody VerifyMasterKeyRequest request,
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

    verifyMasterKeyUseCase.execute(
        resolveAccountId(authenticatedAccount),
        authenticatedAccount.sessionId(),
        request);

    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Master Key verificada exitosamente")
            .build());
  }

  /**
   * Registra una nueva credencial en el gestor del usuario autenticado.
   *
   * @param request     DTO con los datos de la credencial.
   * @param authenticatedAccount cuenta autenticada.
   * @return {@code 201 Created} con la respuesta de la credencial creada.
   * @throws NotFoundException si la cuenta no existe.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<PasswordResponse>> createPassword(
      @Validated @RequestBody PasswordRequest request,
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

    UUID accountId = resolveAccountId(authenticatedAccount);
    consume(RateLimitPolicy.VAULT_CRYPTO_SESSION, authenticatedAccount);
    PasswordResponse response = createPasswordUseCase.execute(
        accountId,
        authenticatedAccount.sessionId(),
        request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>(HttpStatus.CREATED.value(),
            "Contraseña guardada exitosamente", response));
  }

  /**
   * Lista todas las credenciales almacenadas del usuario autenticado.
   *
   * @param authenticatedAccount cuenta autenticada.
   * @return {@code 200 OK} con la lista de credenciales.
   * @throws NotFoundException si la cuenta no existe.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<PasswordResponse>>> listPasswords(
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

    UUID accountId = resolveAccountId(authenticatedAccount);
    List<PasswordResponse> response = listPasswordsUseCase.execute(
        accountId,
        authenticatedAccount.sessionId());

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseñas listadas exitosamente", response));
  }

  /**
   * Obtiene una credencial específica con la contraseña descifrada.
   *
   * @param id          UUID de la credencial.
   * @param authenticatedAccount cuenta autenticada.
   * @return {@code 200 OK} con los datos de la credencial.
   * @throws NotFoundException si la credencial no existe o no pertenece a la
   *                           cuenta.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PasswordResponse>> getPassword(
      @PathVariable UUID id,
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

    UUID accountId = resolveAccountId(authenticatedAccount);
    consume(RateLimitPolicy.VAULT_CRYPTO_SESSION, authenticatedAccount);
    PasswordResponse response = getPasswordUseCase.execute(
        accountId,
        authenticatedAccount.sessionId(),
        id);

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseña obtenida exitosamente", response));
  }

  /**
   * Actualiza parcialmente una credencial existente.
   *
   * <p>
   * Solo se modifican los campos enviados en el request. Los campos no
   * incluidos conservan su valor actual.
   *
   * @param id          UUID de la credencial.
   * @param request     DTO con los campos a actualizar.
   * @param authenticatedAccount cuenta autenticada.
   * @return {@code 200 OK} con los datos actualizados.
   * @throws NotFoundException si la credencial no existe o no pertenece a la
   *                           cuenta.
   */
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<PasswordResponse>> updatePassword(
      @PathVariable UUID id,
      @Validated @RequestBody PasswordUpdateRequest request,
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

    UUID accountId = resolveAccountId(authenticatedAccount);
    consume(RateLimitPolicy.VAULT_CRYPTO_SESSION, authenticatedAccount);
    PasswordResponse response = updatePasswordUseCase.execute(
        accountId,
        authenticatedAccount.sessionId(),
        id,
        request);

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseña actualizada exitosamente", response));
  }

  /**
   * Elimina una credencial del gestor.
   *
   * @param id          UUID de la credencial.
   * @param authenticatedAccount cuenta autenticada.
   * @return {@code 200 OK} con confirmación de eliminación.
   * @throws NotFoundException si la credencial no existe o no pertenece a la
   *                           cuenta.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePassword(
      @PathVariable UUID id,
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount) {

    UUID accountId = resolveAccountId(authenticatedAccount);
    deletePasswordUseCase.execute(
        accountId,
        authenticatedAccount.sessionId(),
        id);

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseña eliminada exitosamente", null));
  }

  private void consume(
      RateLimitPolicy policy,
      AuthenticatedAccount account) {
    rateLimitGuard.consume(policy, rateLimitSubject(account));
  }

  private String rateLimitSubject(AuthenticatedAccount account) {
    return account.accountId() + ":" + account.sessionId();
  }
}
