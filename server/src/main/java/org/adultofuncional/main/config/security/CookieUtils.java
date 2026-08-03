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

  public static final String ACCESS_TOKEN_COOKIE = "token";
  public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
  public static final String REFRESH_TOKEN_PATH = "/api/auth/refresh";

  /**
   * Activa el atributo {@code Secure} en la cookie, restringiendo su envío
   * a conexiones HTTPS. Configurado via {@code APP_COOKIE_SECURE}.
   *
   * <ul>
   * <li>{@code false} — desarrollo local (HTTP permitido)</li>
   * <li>{@code true} — producción (solo HTTPS); obligatorio si
   * {@code APP_COOKIE_SAME_SITE=None}</li>
   * </ul>
   */
  private final boolean appCookieSecure;

  /**
   * Valor del atributo {@code SameSite} de la cookie. Configurable via
   * {@code APP_COOKIE_SAME_SITE}. Valores válidos: {@code Strict},
   * {@code Lax}, {@code None}.
   *
   * <p>
   * Se aplica tanto al establecer como al eliminar la cookie para garantizar
   * que el navegador procese correctamente el {@code Set-Cookie} en ambos casos.
   */
  private final String appCookieSameSite;

  /**
   * Valida la política antes de aceptar tráfico HTTP.
   *
   * <p>{@code SameSite=None} sin {@code Secure} se rechaza porque los
   * navegadores modernos no aceptan esa cookie. Un valor desconocido también
   * detiene el arranque para evitar degradar silenciosamente la protección.</p>
   */
  public CookieUtils(
      @Value("${APP_COOKIE_SECURE}") boolean appCookieSecure,
      @Value("${APP_COOKIE_SAME_SITE}") String appCookieSameSite) {
    this.appCookieSecure = appCookieSecure;
    this.appCookieSameSite = normalizeSameSite(appCookieSameSite);
    if ("None".equals(this.appCookieSameSite) && !appCookieSecure) {
      throw new IllegalStateException(
          "APP_COOKIE_SAME_SITE=None requiere APP_COOKIE_SECURE=true");
    }
  }

  /** Expone la política Secure para configurar también la cookie CSRF. */
  public boolean isSecure() {
    return appCookieSecure;
  }

  /** Expone la política SameSite validada para la cookie CSRF. */
  public String sameSite() {
    return appCookieSameSite;
  }

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
    addCookie(response, ACCESS_TOKEN_COOKIE, token, "/", expirationMs);
  }

  /** Agrega el refresh token restringido exclusivamente a su endpoint. */
  public void addRefreshTokenCookie(HttpServletResponse response, String token, long expirationMs) {
    addCookie(response, REFRESH_TOKEN_COOKIE, token, REFRESH_TOKEN_PATH, expirationMs);
  }

  /** Agrega en una sola operación las dos cookies de la familia. */
  public void addAuthenticationCookies(
      HttpServletResponse response,
      String accessToken,
      long accessExpirationMs,
      String refreshToken,
      long refreshExpirationMs) {
    addTokenCookie(response, accessToken, accessExpirationMs);
    addRefreshTokenCookie(response, refreshToken, refreshExpirationMs);
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
    clearCookie(response, ACCESS_TOKEN_COOKIE, "/");
  }

  /** Elimina la cookie de refresh conservando exactamente su Path. */
  public void clearRefreshTokenCookie(HttpServletResponse response) {
    clearCookie(response, REFRESH_TOKEN_COOKIE, REFRESH_TOKEN_PATH);
  }

  /** Elimina ambas credenciales del navegador. */
  public void clearAuthenticationCookies(HttpServletResponse response) {
    clearTokenCookie(response);
    clearRefreshTokenCookie(response);
  }

  private void addCookie(
      HttpServletResponse response,
      String name,
      String value,
      String path,
      long expirationMs) {
    response.addHeader("Set-Cookie",
        String.format("%s=%s; HttpOnly; %sPath=%s; Max-Age=%d; SameSite=%s",
            name,
            value,
            appCookieSecure ? "Secure; " : "",
            path,
            Math.max(0L, expirationMs / 1_000L),
            appCookieSameSite));
  }

  private void clearCookie(HttpServletResponse response, String name, String path) {
    response.addHeader("Set-Cookie",
        String.format("%s=; HttpOnly; %sPath=%s; Max-Age=0; SameSite=%s",
            name,
            appCookieSecure ? "Secure; " : "",
            path,
            appCookieSameSite));
  }

  private String normalizeSameSite(String value) {
    if (value == null) {
      throw new IllegalStateException("APP_COOKIE_SAME_SITE es obligatorio");
    }
    return switch (value.strip().toLowerCase(java.util.Locale.ROOT)) {
      case "strict" -> "Strict";
      case "lax" -> "Lax";
      case "none" -> "None";
      default -> throw new IllegalStateException(
          "APP_COOKIE_SAME_SITE debe ser Strict, Lax o None");
    };
  }
}
