package org.adultofuncional.main.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autenticación JWT que intercepta cada request HTTP una única vez.
 *
 * <p>
 * Extiende {@link OncePerRequestFilter} para garantizar una sola ejecución por
 * request. Extrae el token JWT del header {@code Authorization} o de la cookie
 * {@code HttpOnly} llamada {@code token}, lo valida y establece el contexto de
 * seguridad de Spring si el token es válido.
 *
 * <p>
 * <strong>Estrategia de extracción del token:</strong>
 * <ol>
 * <li>Busca primero en el header {@code Authorization: Bearer <token>}</li>
 * <li>Si no lo encuentra, busca en la cookie {@code token} (HttpOnly)</li>
 * <li>Si no hay token, continúa la cadena sin autenticar</li>
 * </ol>
 *
 * <p>
 * El token preferido es la cookie HttpOnly,
 * que protege contra XSS. El soporte para header {@code Authorization} se
 * mantiene
 * para compatibilidad con clientes que no soporten cookies (ej. clientes
 * móviles o CLI).
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtService
 * @see CookieUtils
 * @see SecurityConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /** Servicio para parsear y validar tokens JWT. */
  private final JwtService jwtService;

  /**
   * Lógica principal del filtro. Extrae, valida el JWT y establece el contexto
   * de seguridad si el token es válido.
   *
   * @param request     request HTTP entrante
   * @param response    response HTTP saliente
   * @param filterChain cadena de filtros de Spring Security
   * @throws ServletException si ocurre un error en el procesamiento del servlet
   * @throws IOException      si ocurre un error de I/O
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    // 1. Busca el token — primero en header, luego en cookie
    String jwt = extractFromHeader(request);
    if (jwt == null) {
      jwt = extractFromCookie(request);
    }

    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Claims claims = jwtService.parseAndValidate(jwt);
      String userEmail = claims.get("email", String.class);

      if (userEmail != null &&
          SecurityContextHolder.getContext().getAuthentication() == null) {

        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles == null
            ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
            : roles.stream().map(SimpleGrantedAuthority::new).toList();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEmail, null,
            authorities);

        authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

    } catch (ExpiredJwtException e) {
      log.debug("Token JWT expirado para request {}: {}", request.getRequestURI(), e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT expirado");
      return;
    } catch (SignatureException e) {
      log.warn("Firma JWT inválida en request {}: {}", request.getRequestURI(), e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Firma JWT inválida");
      return;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token JWT inválido en request {}: {}", request.getRequestURI(), e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido");
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extrae el JWT del header {@code Authorization} si tiene el formato
   * {@code Bearer <token>}.
   *
   * @param request request HTTP entrante
   * @return el token JWT sin el prefijo {@code Bearer }, o {@code null} si no
   *         está presente
   */
  private String extractFromHeader(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  /**
   * Extrae el JWT de la cookie {@code HttpOnly} llamada {@code token}.
   *
   * @param request request HTTP entrante
   * @return el valor de la cookie {@code token}, o {@code null} si no existe
   */
  private String extractFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null)
      return null;
    return Arrays.stream(cookies)
        .filter(c -> "token".equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
