package org.adultofuncional.main.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

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
 * Filtro de autenticación JWT que intercepta cada request HTTP exactamente
 * una vez por ciclo de vida del request.
 *
 * <p>
 * Extiende {@link OncePerRequestFilter} para garantizar una única ejecución
 * incluso en cadenas de filtros complejas. Extrae el token JWT desde el
 * header {@code Authorization} o desde la cookie {@code HttpOnly} llamada
 * {@code token}, lo valida mediante {@link JwtService} y, si es válido,
 * establece el contexto de autenticación de Spring Security para el resto
 * del ciclo del request.
 *
 * <p>
 * <strong>Orden de extracción del token:</strong>
 * <ol>
 * <li>Header {@code Authorization: Bearer <token>} — usado por clientes
 * nativos (móvil/desktop) que almacenan el token fuera del navegador.</li>
 * <li>Cookie {@code token} (HttpOnly) — usado por clientes web; protege
 * el token contra acceso desde JavaScript (XSS).</li>
 * <li>Si ninguna fuente contiene un token, el request continúa sin
 * autenticar y Spring Security aplicará las reglas de autorización
 * definidas en {@link SecurityConfig}.</li>
 * </ol>
 *
 * <p>
 * <strong>Manejo de errores:</strong> los errores de validación no se
 * propagan como excepciones — se interceptan aquí y se convierten en
 * respuestas {@code 401 Unauthorized} con formato {@link ApiResponse},
 * manteniendo la consistencia con el resto de la API y evitando que el
 * {@code GlobalExceptionHandler} deba conocer detalles de JWT.
 * Los tokens expirados se loguean en {@code DEBUG}; las firmas inválidas
 * en {@code WARN}, ya que pueden indicar un intento de manipulación.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtService
 * @see CookieUtils
 * @see SecurityConfig
 * @see ClientTypeResolver
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /**
   * Nombre de la cookie HttpOnly que transporta el JWT en clientes web.
   * Debe coincidir con el nombre usado en {@link CookieUtils}.
   */
  private static final String TOKEN_COOKIE_NAME = "token";

  /**
   * Prefijo estándar del header Authorization para tokens Bearer.
   * El token JWT comienza en el índice 7 (longitud de "Bearer ").
   */
  private static final String BEARER_PREFIX = "Bearer ";

  /** Servicio que parsea, firma y valida tokens JWT. */
  private final JwtService jwtService;

  /** Serializador JSON para escribir respuestas de error uniformes. */
  private final ObjectMapper objectMapper;

  /**
   * Ejecuta la lógica de autenticación JWT para cada request entrante.
   *
   * <p>
   * <strong>Flujo completo:</strong>
   * <ol>
   * <li>Intenta extraer el JWT del header {@code Authorization}.</li>
   * <li>Si no hay header, intenta extraerlo de la cookie {@code token}.</li>
   * <li>Si no hay token en ninguna fuente, delega al siguiente filtro
   * sin autenticar — Spring Security decidirá si el endpoint requiere
   * autenticación.</li>
   * <li>Si hay token, lo valida con {@link JwtService#parseAndValidate}.</li>
   * <li>Si es válido y no hay autenticación previa en el contexto, construye
   * un {@link UsernamePasswordAuthenticationToken} con el email como
   * principal y los roles como autoridades, y lo registra en el
   * {@link SecurityContextHolder}.</li>
   * <li>Si el token es inválido, responde {@code 401 Unauthorized} con
   * un {@link ApiResponse} consistente, sin continuar la cadena de filtros.</li>
   * </ol>
   *
   * <p>
   * Si el claim {@code roles} está ausente en el token, se asigna
   * {@code ROLE_USER} por defecto para mantener compatibilidad con tokens
   * emitidos antes de que el claim fuera introducido.
   *
   * @param request     request HTTP entrante
   * @param response    response HTTP saliente
   * @param filterChain cadena de filtros de Spring Security
   * @throws ServletException si ocurre un error en el procesamiento del servlet
   * @throws IOException      si ocurre un error de I/O al escribir la respuesta
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

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
      log.debug("Token JWT expirado para request {}: {}",
          request.getRequestURI(), e.getMessage());
      writeUnauthorizedResponse(response, "Token JWT expirado");
      return;
    } catch (SignatureException e) {
      log.warn("Firma JWT inválida en request {} — posible intento de manipulación: {}",
          request.getRequestURI(), e.getMessage());
      writeUnauthorizedResponse(response, "Firma JWT inválida");
      return;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Token JWT malformado o inválido en request {}: {}",
          request.getRequestURI(), e.getMessage());
      writeUnauthorizedResponse(response, "Token JWT inválido");
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Escribe una respuesta 401 Unauthorized con formato {@link ApiResponse}.
   *
   * <p>
   * Centraliza la creación de la respuesta de error para cualquier token
   * inválido, expirado o con firma incorrecta, garantizando que el cliente
   * reciba la misma estructura JSON que en los errores de capa de aplicación.
   *
   * @param response respuesta HTTP donde se escribe el error
   * @param message  mensaje descriptivo para el cliente
   * @throws IOException si ocurre un error al escribir el JSON
   */
  private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
    ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
        .status(HttpStatus.UNAUTHORIZED.value())
        .message(message)
        .build();
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), apiResponse);
  }

  /**
   * Extrae el JWT del header {@code Authorization} si tiene formato Bearer.
   *
   * <p>
   * Formato esperado: {@code Authorization: Bearer eyJhbGci...}. Si el header
   * está ausente o no comienza con {@code "Bearer "}, retorna {@code null}
   * y la extracción continúa con la cookie.
   *
   * @param request request HTTP entrante
   * @return token JWT sin el prefijo {@code "Bearer "}, o {@code null} si
   *         el header está ausente o tiene formato incorrecto
   */
  private String extractFromHeader(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      return authHeader.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  /**
   * Extrae el JWT de la cookie {@code HttpOnly} llamada {@code token}.
   *
   * <p>
   * Si el request no tiene cookies o ninguna se llama {@code token},
   * retorna {@code null}. La cookie es establecida por {@link CookieUtils}
   * en el login y el registro, y eliminada en el logout.
   *
   * @param request request HTTP entrante
   * @return valor de la cookie {@code token}, o {@code null} si no existe
   */
  private String extractFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null)
      return null;
    return Arrays.stream(cookies)
        .filter(c -> TOKEN_COOKIE_NAME.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
