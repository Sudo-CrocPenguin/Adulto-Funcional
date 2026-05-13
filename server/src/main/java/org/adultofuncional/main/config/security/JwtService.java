package org.adultofuncional.main.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

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
 * Utiliza el algoritmo HMAC-SHA256 (HS256) para firmar los tokens. La clave
 * de firma se deriva de la propiedad {@code jwt.secret} (o la variable de
 * entorno {@code JWT_SECRET}) y el tiempo de expiración de
 * {@code jwt.expiration} (o {@code JWT_EXPIRATION}), ambas mapeadas
 * automáticamente por {@link JwtProperties} mediante vinculación relajada
 * de Spring Boot. Si el secreto no cumple el mínimo de 32 caracteres,
 * la aplicación falla al arrancar con {@link IllegalStateException},
 * evitando operar con una clave insegura.
 *
 * <p>
 * <strong>Claims incluidos en cada token:</strong>
 * <ul>
 * <li>{@code sub} — UUID de la cuenta ({@code accountId})</li>
 * <li>{@code email} — correo electrónico del usuario</li>
 * <li>{@code roles} — lista de roles (ej. {@code ["ROLE_USER"]})</li>
 * <li>{@code iat} — timestamp de emisión</li>
 * <li>{@code exp} — timestamp de expiración</li>
 * </ul>
 *
 * <p>
 * <strong>Estrategia de entrega del token:</strong> el token siempre se
 * establece en una cookie {@code HttpOnly} gestionada por {@link CookieUtils}.
 * Adicionalmente, los clientes nativos (móvil/desktop) identificados por
 * {@link ClientTypeResolver} reciben el token también en el body de la
 * respuesta para facilitar su almacenamiento seguro fuera del navegador.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtProperties
 * @see CookieUtils
 * @see JwtAuthenticationFilter
 * @see ClientTypeResolver
 */
@Slf4j
@Service
public class JwtService {

  /**
   * Clave HMAC-SHA256 derivada de {@code jwt.secret}.
   * Se construye una sola vez en el constructor a partir de los valores
   * proporcionados por {@link JwtProperties} y es inmutable.
   */
  private final SecretKey secretKey;

  /**
   * Tiempo de vida del token en milisegundos.
   * Configurado vía {@code jwt.expiration} (ej. {@code 86400000} = 24 horas).
   * Proviene de {@link JwtProperties#getExpiration()}.
   */
  private final long expiration;

  /**
   * Inicializa el servicio derivando la clave de firma y validando que el
   * secreto cumpla el mínimo de seguridad requerido por HS256.
   *
   * @param jwtProperties propiedades de configuración JWT vinculadas
   *                      automáticamente al prefijo {@code jwt}
   * @throws IllegalStateException si {@code jwt.secret} es {@code null} o tiene
   *                               menos de 32 caracteres; la aplicación no
   *                               arrancará en ese caso
   */
  public JwtService(JwtProperties jwtProperties) {
    String secret = jwtProperties.getSecret();
    long expiration = jwtProperties.getExpiration();

    if (secret == null || secret.length() < 32) {
      throw new IllegalStateException(
          "jwt.secret debe tener mínimo 32 caracteres. Longitud actual: "
              + (secret == null ? 0 : secret.length()));
    }

    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  /**
   * Genera un token JWT firmado con los datos de la cuenta.
   *
   * <p>
   * Si {@code authorities} está vacía, el claim {@code roles} se incluye
   * como lista vacía ({@code []}). El token resultante es compacto
   * (formato {@code header.payload.signature}) y está listo para ser
   * almacenado en cookie o enviado en el body.
   *
   * @param accountId   UUID de la cuenta; se almacena como claim {@code sub}
   * @param email       correo electrónico; se almacena como claim {@code email}
   * @param authorities roles del usuario; se mapean a strings en {@code roles}
   * @return token JWT compacto y firmado con HS256
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
   * Extrae el {@code accountId} del claim {@code sub} del token.
   *
   * @param token JWT válido y firmado
   * @return UUID de la cuenta autenticada
   * @throws io.jsonwebtoken.JwtException si el token tiene firma inválida,
   *                                      está expirado o está malformado
   */
  public UUID extractAccountId(String token) {
    return UUID.fromString(extractClaims(token).getSubject());
  }

  /**
   * Extrae el correo electrónico del claim {@code email} del token.
   *
   * @param token JWT válido y firmado
   * @return correo electrónico del usuario autenticado
   * @throws io.jsonwebtoken.JwtException si el token tiene firma inválida,
   *                                      está expirado o está malformado
   */
  public String extractEmail(String token) {
    return extractClaims(token).get("email", String.class);
  }

  /**
   * Extrae los roles del claim {@code roles} del token.
   *
   * @param token JWT válido y firmado
   * @return lista de roles del usuario (ej. {@code ["ROLE_USER"]}),
   *         nunca {@code null}; puede ser vacía
   * @throws io.jsonwebtoken.JwtException si el token tiene firma inválida,
   *                                      está expirado o está malformado
   */
  public List<String> extractRoles(String token) {
    return extractClaims(token).get("roles", List.class);
  }

  /**
   * Retorna el tiempo de expiración configurado en milisegundos.
   *
   * <p>
   * Usado por {@link CookieUtils} para alinear el {@code Max-Age} de la
   * cookie con la vida real del token, evitando que la cookie sobreviva
   * al JWT o viceversa.
   *
   * @return tiempo de expiración en milisegundos
   */
  public long getExpiration() {
    return expiration;
  }

  /**
   * Parsea y valida un token retornando sus claims.
   *
   * <p>
   * Acceso package-private: solo debe ser invocado por
   * {@link JwtAuthenticationFilter} durante la autenticación de cada request.
   * El resto del sistema debe usar los métodos de extracción específicos
   * ({@link #extractEmail}, {@link #extractAccountId}, {@link #extractRoles}).
   *
   * @param token JWT a parsear y validar
   * @return claims del token si la firma y la expiración son válidas
   * @throws io.jsonwebtoken.JwtException si la firma es inválida o el token
   *                                      está expirado
   */
  Claims parseAndValidate(String token) {
    return extractClaims(token);
  }

  /**
   * Parsea el token y retorna sus claims verificando firma y expiración.
   *
   * <p>
   * Este método es el único punto donde se contacta la librería JJWT para
   * validación. Todas las operaciones de extracción lo delegan aquí,
   * centralizando el manejo de errores de parsing en un solo lugar.
   *
   * @param token JWT a parsear
   * @return claims verificados del token
   * @throws io.jsonwebtoken.JwtException si la firma es inválida, el token
   *                                      expiró o el formato es incorrecto
   */
  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
