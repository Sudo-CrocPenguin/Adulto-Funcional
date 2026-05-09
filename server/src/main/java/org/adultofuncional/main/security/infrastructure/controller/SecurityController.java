package org.adultofuncional.main.security.infrastructure.controller;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.PasswordRequest;
import org.adultofuncional.main.security.application.dto.PasswordResponse;
import org.adultofuncional.main.security.application.dto.PasswordUpdateRequest;
import org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.DeletePasswordUseCase;
import org.adultofuncional.main.security.application.usecase.GetPasswordUseCase;
import org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase;
import org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiResponse;
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

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del módulo de gestión de contraseñas.
 *
 * <p>Expone endpoints para crear, consultar, actualizar y eliminar
 * credenciales bajo la ruta base {@code /api/security/passwords}.
 * Delega la lógica de negocio a los casos de uso correspondientes
 * y retorna respuestas envueltas en {@link ApiResponse}.</p>
 *
 * <p>Todos los endpoints resuelven el {@code accountId} del usuario
 * autenticado a partir de su correo electrónico mediante
 * {@link #resolveAccountId(String)}.</p>
 *
 * <p><strong>Seguridad:</strong> Los endpoints que devuelven contraseñas
 * requieren que el usuario haya verificado su Master Key previamente.
 * Esta validación será implementada en los casos de uso correspondientes
 * cuando el servicio AES-256 esté disponible.</p>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 */

@RestController
@RequestMapping("/api/security/passwords")
@RequiredArgsConstructor
public class SecurityController {

    private final CreatePasswordUseCase createPasswordUseCase;
    private final ListPasswordsUseCase listPasswordsUseCase;
    private final GetPasswordUseCase getPasswordUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final DeletePasswordUseCase deletePasswordUseCase;

     /**
     * Repositorio de cuentas utilizado para resolver el UUID de la cuenta
     * a partir del correo electrónico del usuario autenticado.
     */
    private final AccountRepository accountRepository;

    /**
     * Resuelve el identificador único de la cuenta a partir del correo
     * electrónico del usuario autenticado.
     *
     * @param email correo electrónico del usuario autenticado, obtenido
     *              desde el contexto de seguridad mediante {@code @AuthenticationPrincipal}
     * @return UUID de la cuenta asociada al correo electrónico
     * @throws NotFoundException si no existe ninguna cuenta registrada con ese correo
     */

    private UUID resolveAccountId(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada para el email: " + email))
                .getId();
    }

    /**
     * Registra una nueva contraseña en el gestor del usuario autenticado.
     *
     * @param request     objeto {@link PasswordRequest} con los datos validados de la credencial
     * @param loggedEmail correo electrónico del usuario autenticado
     * @return {@link ResponseEntity} con estado {@code 201 Created} y la
     *         {@link PasswordResponse} de la credencial creada
     * @throws NotFoundException si la cuenta del usuario no existe
     */

    @PostMapping
    public ResponseEntity<ApiResponse<PasswordResponse>> createPassword(
            @Validated @RequestBody PasswordRequest request,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        PasswordResponse response = createPasswordUseCase.execute(accountId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(), "Contraseña guardada exitosamente", response));
    }

    /**
     * Lista todas las contraseñas almacenadas del usuario autenticado.
     *
     * @param loggedEmail correo electrónico del usuario autenticado
     * @return {@link ResponseEntity} con estado {@code 200 OK} y la lista de
     *         {@link PasswordResponse} con los datos de cada credencial
     * @throws NotFoundException si la cuenta del usuario no existe
     */

    @GetMapping
    public ResponseEntity<ApiResponse<List<PasswordResponse>>> listPasswords(
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        List<PasswordResponse> response = listPasswordsUseCase.execute(accountId);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseñas listadas exitosamente", response));
    }

    /**
     * Obtiene el detalle de una contraseña específica del usuario autenticado.
     *
     * @param id          UUID de la contraseña a consultar
     * @param loggedEmail correo electrónico del usuario autenticado
     * @return {@link ResponseEntity} con estado {@code 200 OK} y la
     *         {@link PasswordResponse} de la credencial encontrada
     * @throws NotFoundException si la contraseña no existe o no pertenece a la cuenta
     */

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordResponse>> getPassword(
            @PathVariable UUID id,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        PasswordResponse response = getPasswordUseCase.execute(accountId, id);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseña obtenida exitosamente", response));
    }

    /**
     * Actualiza parcialmente una contraseña existente del usuario autenticado.
     *
     * <p>Solo se modifican los campos enviados en el request. Los campos no
     * incluidos conservan su valor actual.
     *
     * @param id          UUID de la contraseña a actualizar
     * @param request     objeto {@link PasswordUpdateRequest} con los campos a modificar
     * @param loggedEmail correo electrónico del usuario autenticado
     * @return {@link ResponseEntity} con estado {@code 200 OK} y la
     *         {@link PasswordResponse} con los datos actualizados
     * @throws NotFoundException si la contraseña no existe o no pertenece a la cuenta
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PasswordResponse>> updatePassword(
            @PathVariable UUID id,
            @Validated @RequestBody PasswordUpdateRequest request,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        PasswordResponse response = updatePasswordUseCase.execute(accountId, id, request);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseña actualizada exitosamente", response));
    }

    /**
     * Elimina una contraseña del gestor del usuario autenticado.
     *
     * @param id          UUID de la contraseña a eliminar
     * @param loggedEmail correo electrónico del usuario autenticado
     * @return {@link ResponseEntity} con estado {@code 200 OK} confirmando la eliminación
     * @throws NotFoundException si la contraseña no existe o no pertenece a la cuenta
     */
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePassword(
            @PathVariable UUID id,
            @AuthenticationPrincipal String loggedEmail) {

        UUID accountId = resolveAccountId(loggedEmail);
        deletePasswordUseCase.execute(accountId, id);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Contraseña eliminada exitosamente", null));
    }
}