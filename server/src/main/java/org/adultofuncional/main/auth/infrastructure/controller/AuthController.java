package org.adultofuncional.main.auth.infrastructure.controller;

import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.usecase.LoginUseCase;
import org.adultofuncional.main.auth.application.usecase.RegisterUseCase;
import org.adultofuncional.main.config.security.ClientTypeResolver;
import org.adultofuncional.main.config.security.CookieUtils;
import org.adultofuncional.main.config.security.JwtService;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  private final CookieUtils cookieUtils;
  private final JwtService jwtService;
  private final ClientTypeResolver clientTypeResolver;

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

    cookieUtils.addTokenCookie(response, auth.getToken(), jwtService.getExpiration());

    AuthResponse responseData = clientTypeResolver.isNativeClient(httpRequest)
        ? auth
        : auth.withoutToken();

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
   * ya está registrado, el caso de uso lanza una excepción de conflicto (409)
   * que el {@code GlobalExceptionHandler} convierte en la respuesta adecuada.
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

    cookieUtils.addTokenCookie(response, auth.getToken(), jwtService.getExpiration());

    AuthResponse responseData = clientTypeResolver.isNativeClient(httpRequest)
        ? auth
        : auth.withoutToken();

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
            ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Cuenta creada exitosamente")
                .data(responseData)
                .build());
  }

  /**
   * Cierra la sesión del usuario eliminando la cookie de autenticación.
   *
   * <p>
   * Instruye al navegador a invalidar la cookie {@code token} estableciendo
   * {@code Max-Age=0}. No requiere body ni autenticación activa — es seguro
   * llamarlo aunque el token ya haya expirado.
   *
   * @param response respuesta HTTP donde se escribe el header de limpieza
   * @return 204 No Content
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    cookieUtils.clearTokenCookie(response);
    return ResponseEntity.noContent().build();
  }
}
