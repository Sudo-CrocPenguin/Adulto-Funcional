package org.adultofuncional.main.config.security;

import java.util.Arrays;
import java.util.Set;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Aplica CSRF únicamente cuando el navegador autentica una operación insegura.
 *
 * <p>Un Bearer previamente validado por {@link JwtAuthenticationFilter}
 * identifica a un cliente nativo y no requiere CSRF. La mera presencia de un
 * header {@code Authorization} no basta: el filtro JWT debe haber marcado el
 * request como Bearer válido. Las cookies de acceso o refresh sí activan la
 * protección porque el navegador las envía automáticamente.</p>
 */
@Component
public class CookieAuthenticatedCsrfMatcher implements RequestMatcher {

  private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "TRACE", "OPTIONS");

  @Override
  public boolean matches(HttpServletRequest request) {
    if (SAFE_METHODS.contains(request.getMethod())) {
      return false;
    }
    if (JwtAuthenticationFilter.AUTH_SOURCE_BEARER.equals(
        request.getAttribute(JwtAuthenticationFilter.AUTH_SOURCE_ATTRIBUTE))) {
      return false;
    }
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return false;
    }
    return Arrays.stream(cookies)
        .map(Cookie::getName)
        .anyMatch(name -> CookieUtils.ACCESS_TOKEN_COOKIE.equals(name)
            || CookieUtils.REFRESH_TOKEN_COOKIE.equals(name));
  }
}
