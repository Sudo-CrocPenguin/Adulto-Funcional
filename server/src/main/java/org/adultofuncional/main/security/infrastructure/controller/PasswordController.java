package org.adultofuncional.main.security.infrastructure.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.DeletePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.GetPasswordUseCase;
import org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase;
import org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del módulo de gestión de contraseñas.
 *
 * <p>
 * Expone los endpoints para administrar las credenciales almacenadas:
 * crear, listar, obtener (descifrada), actualizar y eliminar, bajo la ruta
 * base {@code /api/security/passwords}. También incluye el endpoint
 * {@code /master-key/verify} para verificar la Master Key del usuario antes
 * de acceder al gestor.
 *
 * <p>
 * El {@code accountId} se resuelve internamente a partir del correo
 * electrónico del usuario autenticado, evitando que el cliente manipule
 * identificadores de cuenta.
 *
 * <p>
 * Todos los endpoints del gestor (excepto la verificación de Master Key)
 * exigen que la Master Key haya sido verificada previamente. Los casos de
 * uso lanzan {@link UnauthorizedException} si no es así.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see CreatePasswordUseCase
 * @see ListPasswordsUseCase
 * @see GetPasswordUseCase
 * @see UpdatePasswordUseCase
 * @see DeletePasswordUseCase
 * @see MasterKeySessionService
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
  private final PasswordEncoder passwordEncoder;
  private final MasterKeySessionService masterKeySessionService;

  /** Repositorio de cuentas para resolver el UUID del usuario autenticado. */
  private final AccountRepository accountRepository;

  /**
   * Resuelve el identificador único de la cuenta a partir del correo
   * electrónico del usuario autenticado.
   *
   * @param email correo electrónico del usuario autenticado, obtenido
   *              mediante {@code @AuthenticationPrincipal}.
   * @return UUID de la cuenta asociada al email.
   * @throws NotFoundException si no existe una cuenta con ese email.
   */
  private UUID resolveAccountId(String email) {
    return accountRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada para el email: " + email))
        .getId();
  }

  /**
   * Verifica la Master Key del usuario autenticado y la mantiene en
   * sesión para las operaciones del gestor.
   *
   * <p>
   * Compara la clave proporcionada con el hash {@code account_master_key}
   * usando {@link PasswordEncoder#matches}. Si la verificación es exitosa,
   * almacena la Master Key en la sesión mediante
   * {@link MasterKeySessionService#verify}.
   *
   * @param body        mapa con la clave {@code masterKey} en texto plano.
   * @param loggedEmail correo electrónico del usuario autenticado.
   * @return {@code 200 OK} si la Master Key es correcta.
   * @throws NotFoundException     si la cuenta no existe.
   * @throws UnauthorizedException si la Master Key es incorrecta o no está
   *                               configurada.
   */
  @PostMapping("/master-key/verify")
  public ResponseEntity<ApiResponse<Void>> verifyMasterKey(
      @RequestBody Map<String, String> body,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    String providedMasterKey = body.get("masterKey");

    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));

    if (account.getMasterKeyHash() == null ||
        !passwordEncoder.matches(providedMasterKey, account.getMasterKeyHash())) {
      throw new UnauthorizedException("Master Key incorrecta");
    }

    masterKeySessionService.verify(accountId, providedMasterKey);

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
   * @param loggedEmail correo del usuario autenticado.
   * @return {@code 201 Created} con la respuesta de la credencial creada.
   * @throws NotFoundException si la cuenta no existe.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<PasswordResponse>> createPassword(
      @Validated @RequestBody PasswordRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    PasswordResponse response = createPasswordUseCase.execute(accountId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>(HttpStatus.CREATED.value(),
            "Contraseña guardada exitosamente", response));
  }

  /**
   * Lista todas las credenciales almacenadas del usuario autenticado.
   *
   * @param loggedEmail correo del usuario autenticado.
   * @return {@code 200 OK} con la lista de credenciales.
   * @throws NotFoundException si la cuenta no existe.
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<PasswordResponse>>> listPasswords(
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    List<PasswordResponse> response = listPasswordsUseCase.execute(accountId);

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseñas listadas exitosamente", response));
  }

  /**
   * Obtiene una credencial específica con la contraseña descifrada.
   *
   * @param id          UUID de la credencial.
   * @param loggedEmail correo del usuario autenticado.
   * @return {@code 200 OK} con los datos de la credencial.
   * @throws NotFoundException si la credencial no existe o no pertenece a la
   *                           cuenta.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PasswordResponse>> getPassword(
      @PathVariable UUID id,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    PasswordResponse response = getPasswordUseCase.execute(accountId, id);

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
   * @param loggedEmail correo del usuario autenticado.
   * @return {@code 200 OK} con los datos actualizados.
   * @throws NotFoundException si la credencial no existe o no pertenece a la
   *                           cuenta.
   */
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<PasswordResponse>> updatePassword(
      @PathVariable UUID id,
      @Validated @RequestBody PasswordUpdateRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    PasswordResponse response = updatePasswordUseCase.execute(accountId, id, request);

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseña actualizada exitosamente", response));
  }

  /**
   * Elimina una credencial del gestor.
   *
   * @param id          UUID de la credencial.
   * @param loggedEmail correo del usuario autenticado.
   * @return {@code 200 OK} con confirmación de eliminación.
   * @throws NotFoundException si la credencial no existe o no pertenece a la
   *                           cuenta.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePassword(
      @PathVariable UUID id,
      @AuthenticationPrincipal String loggedEmail) {

    UUID accountId = resolveAccountId(loggedEmail);
    deletePasswordUseCase.execute(accountId, id);

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(),
            "Contraseña eliminada exitosamente", null));
  }
}
