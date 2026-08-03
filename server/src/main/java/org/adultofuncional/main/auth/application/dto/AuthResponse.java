package org.adultofuncional.main.auth.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
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
 * <li>Los navegadores reciben access y refresh en cookies HttpOnly y una copia
 * de este DTO sin tokens mediante {@link #withoutToken()}.</li>
 * <li>Los clientes nativos reciben access y refresh en el DTO y los almacenan
 * mediante las capacidades seguras de su plataforma.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see CookieUtils
 * @see org.adultofuncional.main.config.security.ClientTypeResolver
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  /**
   * Access JWT perteneciente a una familia de sesión revocable.
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
   * <li>{@code sid} — ID de sesión</li>
   * <li>{@code jti} — ID del access token</li>
   * <li>{@code email} — correo electrónico</li>
   * <li>{@code roles} — roles del usuario</li>
   * <li>{@code iss}/{@code aud} — emisor y audiencia</li>
   * <li>{@code iat} — timestamp de emisión</li>
   * <li>{@code exp} — timestamp de expiración</li>
   * </ul>
   */
  private String token;

  /** Refresh token opaco; solo se incluye para clientes nativos. */
  private String refreshToken;

  /**
   * Tipo de token ({@code "Bearer"}) cuando el DTO transporta credenciales.
   * En respuestas web se omite junto con los tokens.
   */
  @Builder.Default
  private String tokenType = "Bearer";

  /**
   * Tiempo de expiración del token en milisegundos.
   * Útil para que el frontend sepa cuándo debe solicitar un nuevo token
   * (refresh token) o redirigir al login.
   */
  private Long expiresIn;

  /** Tiempo restante del refresh token en milisegundos. */
  private Long refreshExpiresIn;

  /** Identificador de la familia de autenticación creada. */
  private UUID sessionId;

  /** Autoridades persistidas que fueron incluidas en el access token. */
  @Builder.Default
  private List<String> roles = List.of();

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
  private Instant createdAt;

  /**
   * Indica si el usuario tiene configurada una Master Key.
   * Si es {@code true}, el usuario puede acceder al gestor de contraseñas.
   * Si es {@code false}, el frontend puede mostrar una opción para configurarla.
   */
  private boolean hasMasterKey;

  /** Construye la respuesta canónica a partir de cuenta y par de sesión. */
  public static AuthResponse from(Account account, SessionTokens tokens, Instant now) {
    return AuthResponse.builder()
        .token(tokens.accessToken())
        .refreshToken(tokens.refreshToken())
        .tokenType("Bearer")
        .expiresIn(tokens.accessExpiresInMillis(now))
        .refreshExpiresIn(tokens.refreshExpiresInMillis(now))
        .sessionId(tokens.sessionId())
        .roles(tokens.roles())
        .accountId(account.getId())
        .names(account.getNames())
        .lastnames(account.getLastnames())
        .email(account.getEmail())
        .phone(account.getPhone())
        .createdAt(account.getCreatedAt())
        .hasMasterKey(account.getMasterKeyHash() != null)
        .build();
  }

  /**
   * Retorna una copia de este objeto sin el token JWT.
   * Usado para no exponer el token en el body de la respuesta
   * cuando se usa HttpOnly cookie.
   */
  public AuthResponse withoutToken() {
    return AuthResponse.builder()
        .token(null)
        .refreshToken(null)
        .tokenType(null)
        .expiresIn(this.expiresIn)
        .refreshExpiresIn(this.refreshExpiresIn)
        .sessionId(this.sessionId)
        .roles(this.roles)
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
