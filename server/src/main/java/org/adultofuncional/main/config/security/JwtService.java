package org.adultofuncional.main.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio responsable de generar, firmar y validar JSON Web Tokens (JWT).
 *
 * <p>
 * Usa el algoritmo HMAC-SHA256 (HS256) para firmar los tokens.
 * Los tokens contienen el {@code accountId} como subject y el {@code email}
 * como claim adicional.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Slf4j
@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expiration;

  /**
   * Constructor que inicializa la clave de firma y el tiempo de expiración
   * desde las variables de entorno definidas en application.yml / .env
   *
   * @param secret     clave secreta en texto plano (mínimo 32 caracteres
   *                   recomendado)
   * @param expiration tiempo de vida del token en milisegundos (ej: 86400000 =
   *                   24h)
   */
  public JwtService(
      @Value("${JWT_SECRET}") String secret,
      @Value("${JWT_EXPIRATION}") long expiration) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  /**
   * Genera un token JWT firmado con el accountId y email del usuario.
   *
   * @param accountId identificador UUID de la cuenta (irá como {@code sub})
   * @param email     correo del usuario (claim adicional)
   * @return token JWT como String
   */
  public String generateToken(String accountId, String email) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .subject(accountId) // sub = accountId
        .claim("email", email) // claim extra
        .issuedAt(now) // iat
        .expiration(expiryDate) // exp
        .signWith(secretKey) // firma con HS256
        .compact();
  }

  /**
   * Extrae el accountId (subject) de un token JWT.
   *
   * @param token JWT del que extraer el subject
   * @return UUID del account
   */
  public UUID extractAccountId(String token) {
    return UUID.fromString(extractClaims(token).getSubject());
  }

  /**
   * Extrae el email del token JWT.
   *
   * @param token JWT del que extraer el email
   * @return email del usuario
   */
  public String extractEmail(String token) {
    return extractClaims(token).get("email", String.class);
  }

  /**
   * Valida que el token sea correcto: firma válida y no expirado.
   *
   * @param token JWT a validar
   * @return {@code true} si el token es válido
   */
  public boolean isTokenValid(String token) {
    try {
      extractClaims(token); // lanza excepción si es inválido
      return true;
    } catch (Exception e) {
      log.warn("Token JWT inválido: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Retorna el tiempo de expiración configurado en milisegundos.
   * Usado por los use cases para incluirlo en {@code AuthResponse}.
   *
   * @return tiempo de expiración en ms
   */
  public long getExpiration() {
    return expiration;
  }

  // ── privado ────────────────────────────────────────────────────────────

  /**
   * Parsea y valida el token, retornando sus claims.
   * Lanza excepción si la firma es inválida o el token expiró.
   */
  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
