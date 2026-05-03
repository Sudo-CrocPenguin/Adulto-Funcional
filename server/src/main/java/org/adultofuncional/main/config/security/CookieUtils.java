package org.adultofuncional.main.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utilidad para gestionar las cookies de autenticación JWT.
 *
 * <p>
 * Centraliza la creación y eliminación de la cookie {@code token}, que almacena
 * el JWT del usuario de forma segura mediante los atributos {@code HttpOnly},
 * {@code SameSite=Strict} y opcionalmente {@code Secure} (en producción con
 * HTTPS).
 *
 * <p>
 * El uso de {@code HttpOnly} impide que JavaScript acceda a la cookie,
 * protegiéndola
 * contra ataques XSS. El atributo {@code SameSite=Strict} mitiga ataques CSRF
 * al
 * restringir el envío de la cookie a peticiones del mismo origen.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtService
 * @see JwtAuthenticationFilter
 */
@Component
public class CookieUtils {

  /**
   * Indica si la cookie debe incluir el atributo {@code Secure}.
   * Se configura mediante la variable de entorno {@code COOKIE_SECURE}.
   *
   * <ul>
   * <li>{@code false} — desarrollo local (HTTP permitido)</li>
   * <li>{@code true} — producción (solo HTTPS)</li>
   * </ul>
   *
   */
  // TODO: Asegurarse de que COOKIE_SECURE=true en el entorno
  // de producción.
  @Value("${COOKIE_SECURE}")
  private boolean secure;

  /**
   * Agrega la cookie {@code token} a la respuesta HTTP con el JWT del usuario.
   *
   * <p>
   * La cookie se configura con los siguientes atributos de seguridad:
   * <ul>
   * <li>{@code HttpOnly} — inaccesible desde JavaScript</li>
   * <li>{@code Secure} — solo en HTTPS (si {@code COOKIE_SECURE=true})</li>
   * <li>{@code Path=/} — disponible en toda la aplicación</li>
   * <li>{@code Max-Age} — tiempo de vida derivado de {@code JWT_EXPIRATION}</li>
   * <li>{@code SameSite=Strict} — protección contra CSRF</li>
   * </ul>
   *
   * @param response     respuesta HTTP donde se agrega el header
   *                     {@code Set-Cookie}
   * @param token        JWT generado por {@link JwtService#generateToken}
   * @param expirationMs tiempo de vida del token en milisegundos (se convierte a
   *                     segundos)
   */
  public void addTokenCookie(HttpServletResponse response, String token, long expirationMs) {
    response.addHeader("Set-Cookie",
        String.format("token=%s; HttpOnly; %sPath=/; Max-Age=%d; SameSite=Strict",
            token,
            secure ? "Secure; " : "",
            (int) (expirationMs / 1000)));
  }

  /**
   * Elimina la cookie {@code token} de la respuesta HTTP.
   *
   * <p>
   * Establece {@code Max-Age=0} para que el navegador invalide y elimine
   * la cookie inmediatamente. Se usa al cerrar sesión
   * ({@code POST /api/auth/logout}).
   *
   * @param response respuesta HTTP donde se agrega el header {@code Set-Cookie}
   *                 de limpieza
   */
  public void clearTokenCookie(HttpServletResponse response) {
    response.addHeader("Set-Cookie",
        "token=; HttpOnly; Path=/; Max-Age=0; SameSite=Strict");
  }
}
