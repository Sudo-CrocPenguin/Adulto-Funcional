package org.adultofuncional.main.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de autenticación.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula la información devuelta al cliente después de
 * una operación exitosa de autenticación (login o registro).
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para enviar al frontend el token JWT necesario para las peticiones
 * autenticadas, junto con los datos básicos de la cuenta del usuario.
 * Excluye deliberadamente campos sensibles como contraseña y clave maestra.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Después de que {@code LoginUseCase} o {@code RegisterUseCase} autentican
 * o crean un usuario, generan un token JWT y lo combinan con los datos
 * de la cuenta (mapeados desde {@code Account}) para formar este DTO.
 * El controlador lo envuelve en un {@code ApiResponse} y lo retorna al cliente.
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code token} - Token JWT para autenticación en endpoints protegidos</li>
 *   <li>{@code tokenType} - Tipo de token (normalmente "Bearer")</li>
 *   <li>{@code expiresIn} - Tiempo de expiración del token en milisegundos</li>
 *   <li>{@code accountId} - Identificador UUID de la cuenta</li>
 *   <li>{@code names} - Nombres del usuario</li>
 *   <li>{@code lastnames} - Apellidos del usuario</li>
 *   <li>{@code email} - Correo electrónico</li>
 *   <li>{@code phone} - Teléfono de contacto</li>
 *   <li>{@code createdAt} - Fecha de creación de la cuenta</li>
 *   <li>{@code hasMasterKey} - Indica si el usuario tiene configurada una Master Key</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong>
 * Este DTO nunca expone la contraseña ni la Master Key. El token JWT contiene
 * información mínima (generalmente solo el ID y email) y es firmado.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.auth.application.usecase.LoginUseCase
 * @see org.adultofuncional.main.auth.application.usecase.RegisterUseCase
 * @see org.adultofuncional.main.config.security.JwtService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * Token JWT (JSON Web Token) para autenticación stateless.
     * El cliente debe incluirlo en el header {@code Authorization: Bearer <token>}
     * para todas las peticiones a endpoints protegidos.
     *
     * <p><strong>Contenido típico del token:</strong>
     * <ul>
     *   <li>{@code sub} (subject) - ID de la cuenta</li>
     *   <li>{@code email} - Correo electrónico del usuario</li>
     *   <li>{@code iat} (issued at) - Fecha de emisión</li>
     *   <li>{@code exp} (expiration) - Fecha de expiración</li>
     * </ul>
     */
    private String token;

    /**
     * Tipo de token. Siempre {@code "Bearer"} para autenticación JWT estándar.
     * Indica al cliente cómo debe enviar el token en las peticiones.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Tiempo de expiración del token en milisegundos.
     * Útil para que el frontend sepa cuándo debe solicitar un nuevo token
     * (refresh token) o redirigir al login.
     */
    private Long expiresIn;

    /**
     * Identificador único de la cuenta (UUID v7).
     * Corresponde al campo {@code account_id} en la base de datos.
     */
    private UUID accountId;

    /**
     * Nombres del titular de la cuenta.
     * Corresponde a {@code account_names}.
     */
    private String names;

    /**
     * Apellidos del titular de la cuenta.
     * Corresponde a {@code account_lastnames}.
     */
    private String lastnames;

    /**
     * Correo electrónico del usuario (único en el sistema).
     * Corresponde a {@code account_email}.
     */
    private String email;

    /**
     * Número de teléfono de contacto.
     * Corresponde a {@code account_phone}.
     */
    private String phone;

    /**
     * Fecha y hora de creación de la cuenta.
     * Corresponde a {@code account_created_at}.
     */
    private LocalDateTime createdAt;

    /**
     * Indica si el usuario tiene configurada una Master Key.
     * Si es {@code true}, el usuario puede acceder al gestor de contraseñas.
     * Si es {@code false}, el frontend puede mostrar una opción para configurarla.
     *
     * //TODO: Agregar campos adicionales para roles o permisos en el futuro
     */
    private boolean hasMasterKey;
}