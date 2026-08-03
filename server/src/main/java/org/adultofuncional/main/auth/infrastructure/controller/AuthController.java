package org.adultofuncional.main.auth.infrastructure.controller;

import java.util.Arrays;

import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.CsrfResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RefreshRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.usecase.LoginUseCase;
import org.adultofuncional.main.auth.application.usecase.RefreshSessionUseCase;
import org.adultofuncional.main.auth.application.usecase.RegisterUseCase;
import org.adultofuncional.main.auth.application.usecase.RevokeAllSessionsUseCase;
import org.adultofuncional.main.auth.application.usecase.RevokeCurrentSessionUseCase;
import org.adultofuncional.main.config.security.AuthenticatedAccount;
import org.adultofuncional.main.config.security.ClientTypeResolver;
import org.adultofuncional.main.config.security.CookieUtils;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del módulo de autenticación.
 *
 * <p>
 * Expone los endpoints públicos para login, registro y logout de usuarios.
 * Delega la lógica de negocio a los casos de uso correspondientes y
 * coordina la entrega del JWT según el tipo de cliente detectado por
 * {@link ClientTypeResolver}.
 *
 * <p>
 * <strong>Estrategia de entrega del JWT:</strong>
 * <ul>
 * <li>El token siempre se establece en una cookie {@code HttpOnly} mediante
 * {@link CookieUtils}, independientemente del tipo de cliente.</li>
 * <li>Los clientes nativos (móvil/desktop) identificados por
 * {@link ClientTypeResolver#isNativeClient} reciben además el token
 * en el body de la respuesta para facilitar su almacenamiento fuera
 * del navegador.</li>
 * <li>Los clientes web reciben el body sin token — deben usar la cookie.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * Los DTOs de entrada ({@link LoginRequest}, {@link RegisterRequest}) están
 * anotados con {@code @NoHtml} para rechazar cualquier intento de almacenar
 * scripts maliciosos.
 *
 * <p>
 * Todas las rutas están bajo el prefijo {@code /api/auth} y son públicas
 * (no requieren autenticación previa). Ver
 * {@link org.adultofuncional.main.config.security.SecurityConfig}.
 *
 * @author Lydis Esther Jaraba, Juan Sebastian Rios
 * @since 0.0.1
 * @see LoginUseCase
 * @see RegisterUseCase
 * @see ClientTypeResolver
 * @see CookieUtils
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final RegisterUseCase registerUseCase;
  private final RefreshSessionUseCase refreshSessionUseCase;
  private final RevokeCurrentSessionUseCase revokeCurrentSessionUseCase;
  private final RevokeAllSessionsUseCase revokeAllSessionsUseCase;
  private final CookieUtils cookieUtils;
  private final ClientTypeResolver clientTypeResolver;

  /** Materializa el token CSRF y su cookie para clientes web. */
  @GetMapping("/csrf")
  public ResponseEntity<ApiResponse<CsrfResponse>> csrf(CsrfToken csrfToken) {
    return ResponseEntity.ok(ApiResponse.<CsrfResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Token CSRF generado")
        .data(new CsrfResponse(
            csrfToken.getToken(),
            csrfToken.getHeaderName(),
            csrfToken.getParameterName()))
        .build());
  }

  /**
   * Inicia sesión con las credenciales del usuario.
   *
   * <p>
   * Delega la verificación de credenciales al {@link LoginUseCase}. Si son
   * válidas, establece el JWT en la cookie {@code HttpOnly} y retorna los
   * datos de la cuenta. El token se incluye en el body solo si el request
   * proviene de un cliente nativo según
   * {@link ClientTypeResolver#isNativeClient}.
   *
   * @param request     credenciales del usuario (email y contraseña)
   * @param httpRequest request HTTP para detectar el tipo de cliente
   * @param response    respuesta HTTP donde se escribe la cookie
   * @return 200 OK con los datos de la cuenta; token en body solo para
   *         clientes nativos
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {

    AuthResponse auth = loginUseCase.execute(request);
    writeNoStoreHeaders(response);
    boolean nativeClient = clientTypeResolver.isNativeClient(httpRequest);
    if (!nativeClient) {
      writeAuthenticationCookies(response, auth);
    }
    AuthResponse responseData = nativeClient ? auth : auth.withoutToken();

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Inicio de sesión exitoso")
            .data(responseData)
            .build());
  }

  /**
   * Registra un nuevo usuario en la aplicación.
   *
   * <p>
   * Delega la creación de la cuenta al {@link RegisterUseCase}. Si el email
   * ya está registrado, el caso de uso lanza una
   * {@link org.adultofuncional.main.shared.exception.ConflictException} (HTTP
   * 409).
   * Si el registro es exitoso, establece el JWT en cookie y retorna los datos
   * de la cuenta recién creada.
   *
   * @param request     datos del nuevo usuario (nombre, email, contraseña, etc.)
   * @param httpRequest request HTTP para detectar el tipo de cliente
   * @param response    respuesta HTTP donde se escribe la cookie
   * @return 201 CREATED con los datos de la cuenta; token en body solo para
   *         clientes nativos
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {

    AuthResponse auth = registerUseCase.execute(request);
    writeNoStoreHeaders(response);
    boolean nativeClient = clientTypeResolver.isNativeClient(httpRequest);
    if (!nativeClient) {
      writeAuthenticationCookies(response, auth);
    }
    AuthResponse responseData = nativeClient ? auth : auth.withoutToken();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.<AuthResponse>builder()
            .status(HttpStatus.CREATED.value())
            .message("Cuenta creada exitosamente")
            .data(responseData)
            .build());
  }

  /**
   * Cierra la sesión del usuario eliminando la cookie de autenticación y la
   * Master Key verificada en el gestor de contraseñas.
   *
   * <p>
   * Instruye al navegador a invalidar la cookie {@code token} estableciendo
   * {@code Max-Age=0}. Si el request incluye un JWT válido, también elimina la
   * Master Key efímera asociada a esa cuenta.
   *
   * @param authenticatedAccount principal autenticado cuando el JWT sigue vigente
   * @param response respuesta HTTP donde se escribe el header de limpieza
   * @return 200 OK con un {@link ApiResponse} de confirmación
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
      HttpServletResponse response) {
    if (authenticatedAccount != null) {
      revokeCurrentSessionUseCase.execute(
          authenticatedAccount.accountId(),
          authenticatedAccount.sessionId());
    }
    cookieUtils.clearAuthenticationCookies(response);
    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .status(HttpStatus.OK.value())
            .message("Sesión cerrada exitosamente")
            .build());
  }

  /** Rota el refresh token presentado por cookie web o cuerpo nativo. */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthResponse>> refresh(
      @Valid @RequestBody(required = false) RefreshRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {
    String refreshToken = request != null && request.getRefreshToken() != null
        ? request.getRefreshToken()
        : extractCookie(httpRequest, CookieUtils.REFRESH_TOKEN_COOKIE);
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new UnauthorizedException(
          "Refresh token inválido",
          ApiErrorCode.REFRESH_TOKEN_INVALID);
    }

    AuthResponse auth = refreshSessionUseCase.execute(refreshToken);
    writeNoStoreHeaders(response);
    boolean nativeClient = clientTypeResolver.isNativeClient(httpRequest);
    if (!nativeClient) {
      writeAuthenticationCookies(response, auth);
    }
    AuthResponse responseData = nativeClient ? auth : auth.withoutToken();
    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Sesión renovada exitosamente")
            .data(responseData)
            .build());
  }

  /** Revoca la sesión representada por el access token actual. */
  @DeleteMapping("/sessions/current")
  public ResponseEntity<ApiResponse<Void>> revokeCurrent(
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
      HttpServletResponse response) {
    revokeCurrentSessionUseCase.execute(
        authenticatedAccount.accountId(),
        authenticatedAccount.sessionId());
    cookieUtils.clearAuthenticationCookies(response);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
        .status(HttpStatus.OK.value())
        .message("Sesión actual revocada")
        .build());
  }

  /** Revoca todas las sesiones activas pertenecientes a la cuenta. */
  @DeleteMapping("/sessions")
  public ResponseEntity<ApiResponse<Void>> revokeAll(
      @AuthenticationPrincipal AuthenticatedAccount authenticatedAccount,
      HttpServletResponse response) {
    revokeAllSessionsUseCase.execute(authenticatedAccount.accountId());
    cookieUtils.clearAuthenticationCookies(response);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
        .status(HttpStatus.OK.value())
        .message("Todas las sesiones fueron revocadas")
        .build());
  }

  private void writeAuthenticationCookies(HttpServletResponse response, AuthResponse auth) {
    cookieUtils.addAuthenticationCookies(
        response,
        auth.getToken(),
        auth.getExpiresIn(),
        auth.getRefreshToken(),
        auth.getRefreshExpiresIn());
  }

  private void writeNoStoreHeaders(HttpServletResponse response) {
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
    response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    response.setDateHeader(HttpHeaders.EXPIRES, 0L);
  }

  private String extractCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
