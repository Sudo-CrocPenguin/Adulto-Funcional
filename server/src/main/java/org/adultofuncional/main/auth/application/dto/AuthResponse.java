package org.adultofuncional.main.auth.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.config.security.CookieUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la respuesta de autenticación.
 *
 * <p>
 * Encapsula el token JWT y los datos básicos de la cuenta del usuario
 * tras un login o registro exitoso. Excluye campos sensibles como
 * el hash de contraseña y la master key.
 *
 * <p>
 * <strong>Estrategia de entrega del token:</strong>
 * <ul>
 * <li>El token <b>siempre</b> se establece en una cookie HttpOnly
 * mediante {@link CookieUtils}.</li>
 * <li>Adicionalmente, los clientes nativos (móvil/escritorio)
 * reciben el token en este DTO para su almacenamiento local.
 * Los clientes web reciben una copia sin token mediante
 * {@link #withoutToken()}.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see CookieUtils
 * @see ClientTypeResolver
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  /**
   * Token JWT para autenticación stateless.
   *
   * <p>
   * Se incluye en esta respuesta <b>solo para clientes nativos</b>
   * (móvil/escritorio). Los clientes web reciben el token únicamente
   * en una cookie HttpOnly; en ese caso este campo llega {@code null}
   * (ver {@link #withoutToken()}).
   *
   * <p>
   * <strong>Claims del token:</strong>
   * <ul>
   * <li>{@code sub} — ID de la cuenta</li>
   * <li>{@code email} — correo electrónico</li>
   * <li>{@code roles} — roles del usuario</li>
   * <li>{@code iat} — timestamp de emisión</li>
   * <li>{@code exp} — timestamp de expiración</li>
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

  /**
   * Retorna una copia de este objeto sin el token JWT.
   * Usado para no exponer el token en el body de la respuesta
   * cuando se usa HttpOnly cookie.
   */
  public AuthResponse withoutToken() {
    return AuthResponse.builder()
        .token(null)
        .tokenType(null)
        .expiresIn(this.expiresIn)
        .accountId(this.accountId)
        .names(this.names)
        .lastnames(this.lastnames)
        .email(this.email)
        .phone(this.phone)
        .createdAt(this.createdAt)
        .hasMasterKey(this.hasMasterKey)
        .build();
  }
}
