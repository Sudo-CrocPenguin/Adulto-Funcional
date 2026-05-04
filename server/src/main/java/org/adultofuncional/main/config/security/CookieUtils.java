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
 * {@code SameSite} configurable y opcionalmente {@code Secure} en producción.
 *
 * <p>
 * <strong>Atributos de seguridad:</strong>
 * <ul>
 * <li>{@code HttpOnly} — impide que JavaScript acceda a la cookie,
 * protegiendo el token contra robo mediante XSS.</li>
 * <li>{@code Secure} — restringe la cookie a conexiones HTTPS. Activado
 * mediante {@code APP_COOKIE_SECURE=true} en producción.</li>
 * <li>{@code SameSite} — controla en qué requests cross-site se envía la
 * cookie. Configurable via {@code APP_COOKIE_SAME_SITE}:
 * {@code Strict} para máxima protección CSRF, {@code Lax} para permitir
 * navegación normal entre sitios, {@code None} para requests cross-site
 * (requiere {@code Secure}).</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtService
 * @see JwtAuthenticationFilter
 */
@Component
public class CookieUtils {

  /**
   * Activa el atributo {@code Secure} en la cookie, restringiendo su envío
   * a conexiones HTTPS. Configurado via {@code APP_COOKIE_SECURE}.
   *
   * <ul>
   * <li>{@code false} — desarrollo local (HTTP permitido)</li>
   * <li>{@code true} — producción (solo HTTPS); obligatorio si
   * {@code APP_COOKIE_SAME_SITE=None}</li>
   * </ul>
   *
   * TODO: Verificar que {@code APP_COOKIE_SECURE=true} en producción.
   */
  @Value("${APP_COOKIE_SECURE}")
  private boolean appCookieSecure;

  /**
   * Valor del atributo {@code SameSite} de la cookie. Configurable via
   * {@code APP_COOKIE_SAME_SITE}. Valores válidos: {@code Strict},
   * {@code Lax}, {@code None}.
   *
   * <p>
   * Se aplica tanto al establecer como al eliminar la cookie para garantizar
   * que el navegador procese correctamente el {@code Set-Cookie} en ambos casos.
   */
  @Value("${APP_COOKIE_SAME_SITE}")
  private String appCookieSameSite;

  /**
   * Agrega la cookie {@code token} a la respuesta HTTP con el JWT del usuario.
   *
   * <p>
   * La cookie se construye manualmente via el header {@code Set-Cookie} para
   * poder incluir el atributo {@code SameSite}, que la API de
   * {@link jakarta.servlet.http.Cookie} de Jakarta EE no soporta nativamente.
   *
   * <p>
   * <strong>Atributos aplicados:</strong>
   * <ul>
   * <li>{@code HttpOnly} — siempre activo</li>
   * <li>{@code Secure} — condicional según {@code APP_COOKIE_SECURE}</li>
   * <li>{@code Path=/} — disponible en toda la aplicación</li>
   * <li>{@code Max-Age} — derivado de {@code expirationMs}, alineado con
   * la expiración del JWT para evitar cookies huérfanas</li>
   * <li>{@code SameSite} — según {@code APP_COOKIE_SAME_SITE}</li>
   * </ul>
   *
   * @param response     respuesta HTTP donde se escribe el header
   *                     {@code Set-Cookie}
   * @param token        JWT firmado generado por {@link JwtService#generateToken}
   * @param expirationMs tiempo de vida del token en milisegundos; se convierte
   *                     a segundos para {@code Max-Age}
   */
  public void addTokenCookie(HttpServletResponse response, String token, long expirationMs) {
    response.addHeader("Set-Cookie",
        String.format("token=%s; HttpOnly; %sPath=/; Max-Age=%d; SameSite=%s",
            token,
            appCookieSecure ? "Secure; " : "",
            (int) (expirationMs / 1000),
            appCookieSameSite));
  }

  /**
   * Elimina la cookie {@code token} instruyendo al navegador a invalidarla
   * inmediatamente.
   *
   * <p>
   * Establece {@code Max-Age=0} y un valor vacío, lo que hace que el navegador
   * descarte la cookie en cuanto procesa la respuesta. Se invoca desde
   * {@code POST /api/auth/logout}. Los atributos {@code Secure} y
   * {@code SameSite}
   * deben coincidir con los de la cookie original — de lo contrario algunos
   * navegadores ignoran la instrucción de borrado.
   *
   * @param response respuesta HTTP donde se escribe el header {@code Set-Cookie}
   *                 de invalidación
   */
  public void clearTokenCookie(HttpServletResponse response) {
    response.addHeader("Set-Cookie",
        String.format("token=; HttpOnly; %sPath=/; Max-Age=0; SameSite=%s",
            appCookieSecure ? "Secure; " : "",
            appCookieSameSite));
  }
}
