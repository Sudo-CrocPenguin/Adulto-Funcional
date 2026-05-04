package org.adultofuncional.main.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio responsable de generar, firmar y validar JSON Web Tokens (JWT).
 *
 * <p>
 * Usa el algoritmo HMAC-SHA256 (HS256) para firmar los tokens. Cada token
 * contiene los siguientes claims:
 * <ul>
 * <li>{@code sub} — UUID de la cuenta ({@code accountId})</li>
 * <li>{@code email} — correo electrónico del usuario</li>
 * <li>{@code roles} — lista de roles del usuario (ej.
 * {@code ["ROLE_USER"]})</li>
 * <li>{@code iat} — fecha de emisión</li>
 * <li>{@code exp} — fecha de expiración</li>
 * </ul>
 *
 * <p>
 * {@code JWT_SECRET} debe tener mínimo 32
 * caracteres.
 * El constructor valida esto al arrancar la aplicación y lanza
 * {@link IllegalStateException}
 * si no se cumple, evitando que la aplicación arranque con una clave insegura.
 *
 * <p>
 * El token se almacena en una cookie {@code HttpOnly} gestionada por
 * {@link CookieUtils}
 * y nunca se expone en el body de las respuestas.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see CookieUtils
 * @see JwtAuthenticationFilter
 */
@Slf4j
@Service
public class JwtService {

  /**
   * Clave secreta derivada de {@code JWT_SECRET} para firmar y verificar tokens.
   */
  private final SecretKey secretKey;

  /**
   * Tiempo de vida del token en milisegundos, configurado via
   * {@code JWT_EXPIRATION}.
   */
  private final long expiration;

  /**
   * Constructor que inicializa la clave de firma y el tiempo de expiración
   * desde las variables de entorno.
   *
   * @param secret     clave secreta en texto plano (mínimo 32 caracteres)
   * @param expiration tiempo de vida del token en milisegundos (ej. 3600000 = 1
   *                   hora)
   * @throws IllegalStateException si {@code JWT_SECRET} es nulo o tiene menos de
   *                               32 caracteres
   */
  public JwtService(
      @Value("${JWT_SECRET}") String secret,
      @Value("${JWT_EXPIRATION}") long expiration) {

    if (secret == null || secret.length() < 32) {
      throw new IllegalStateException(
          "JWT_SECRET debe tener mínimo 32 caracteres. Longitud actual: "
              + (secret == null ? 0 : secret.length()));
    }

    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  /**
   * Genera un token JWT firmado con los datos de la cuenta del usuario.
   *
   * @param accountId   UUID de la cuenta (se almacena como {@code sub})
   * @param email       correo electrónico del usuario
   * @param authorities colección de autoridades/roles del usuario
   * @return token JWT compacto y firmado
   */
  public String generateToken(String accountId, String email,
      Collection<? extends GrantedAuthority> authorities) {

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    List<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    return Jwts.builder()
        .subject(accountId)
        .claim("email", email)
        .claim("roles", roles)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Extrae el {@code accountId} (subject) de un token JWT.
   *
   * @param token JWT del que extraer el subject
   * @return UUID de la cuenta
   */
  public UUID extractAccountId(String token) {
    return UUID.fromString(extractClaims(token).getSubject());
  }

  /**
   * Extrae el email del claim {@code email} de un token JWT.
   *
   * @param token JWT del que extraer el email
   * @return correo electrónico del usuario
   */
  public String extractEmail(String token) {
    return extractClaims(token).get("email", String.class);
  }

  /**
   * Extrae la lista de roles del claim {@code roles} de un token JWT.
   *
   * @param token JWT del que extraer los roles
   * @return lista de roles del usuario (ej. {@code ["ROLE_USER"]})
   */
  public List<String> extractRoles(String token) {
    return extractClaims(token).get("roles", List.class);
  }

  /**
   * Retorna el tiempo de expiración configurado en milisegundos.
   * Usado por {@link CookieUtils} para establecer el {@code Max-Age} de la
   * cookie.
   *
   * @return tiempo de expiración en ms
   */
  public long getExpiration() {
    return expiration;
  }

  /**
   * Parsea y valida el token retornando sus claims.
   * Acceso package-private para que solo {@link JwtAuthenticationFilter} lo use.
   *
   * @param token JWT a parsear y validar
   * @return claims del token si la firma y expiración son válidas
   * @throws io.jsonwebtoken.JwtException si la firma es inválida o el token
   *                                      expiró
   */
  Claims parseAndValidate(String token) {
    return extractClaims(token);
  }

  /**
   * Parsea internamente el token y retorna sus claims.
   * Lanza excepción si la firma es inválida o el token expiró.
   *
   * @param token JWT a parsear
   * @return claims del token
   */
  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
