package org.adultofuncional.main.auth.infrastructure.controller;

import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.usecase.LoginUseCase;
import org.adultofuncional.main.auth.application.usecase.RegisterUseCase;
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
 * Expone los endpoints públicos para login y registro de usuarios.
 * Delega la lógica de negocio a los casos de uso correspondientes.
 * Todas las rutas están bajo el prefijo {@code /api/auth}.
 *
 * @author Lydis Esther Jaraba, Juan Sebastian Rios
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final RegisterUseCase registerUseCase;
  private final CookieUtils cookieUtils;
  private final JwtService jwtService;

  /**
   * Header que permite a los clientes no‑navegador solicitar el token JWT
   * en el cuerpo de la respuesta además de (o en lugar de) la cookie.
   *
   * <p>
   * Valores esperados: {@code web} (por defecto), {@code mobile},
   * {@code desktop}. Si el header está ausente o vale {@code web},
   * el token se envía únicamente en la cookie HttpOnly.
   */
  private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

  /**
   * Endpoint para iniciar sesión en la aplicación.
   *
   * <p>
   * Recibe las credenciales del usuario, las valida y delega al
   * {@link LoginUseCase}. Si la autenticación es exitosa, setea el token
   * JWT en una HttpOnly cookie y retorna los datos de la cuenta sin el token.
   *
   * @param request  objeto con email y contraseña del usuario
   * @param response respuesta HTTP donde se agrega la cookie
   * @return 200 OK con datos de la cuenta
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {

    AuthResponse auth = loginUseCase.execute(request);

    // La cookie siempre se establece (el cliente puede ignorarla si no la usa)
    cookieUtils.addTokenCookie(response, auth.getToken(), jwtService.getExpiration());

    // Decide si el token debe aparecer en el body
    AuthResponse responseData = shouldIncludeTokenInBody(httpRequest)
        ? auth // incluye token
        : auth.withoutToken(); // sin token (web)

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Inicio de sesión exitoso")
            .data(responseData)
            .build());
  }

  /**
   * Endpoint para registrar un nuevo usuario en la aplicación.
   *
   * <p>
   * Recibe los datos del formulario de registro, los valida y delega
   * al {@link RegisterUseCase}. Si el email ya está registrado,
   * se lanza una excepción de conflicto (409).
   *
   * @param request  objeto con los datos del nuevo usuario
   * @param response respuesta HTTP donde se agrega la cookie
   * @return 201 CREATED con datos de la cuenta recién creada
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {

    AuthResponse auth = registerUseCase.execute(request);

    // Token va en la cookie HttpOnly — no en el body
    cookieUtils.addTokenCookie(response, auth.getToken(), jwtService.getExpiration());

    AuthResponse responseData = shouldIncludeTokenInBody(httpRequest)
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
   * Endpoint para cerrar sesión.
   *
   * <p>
   * Limpia la cookie del token en el cliente.
   *
   * @param response respuesta HTTP donde se limpia la cookie
   * @return 204 No Content
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    cookieUtils.clearTokenCookie(response);
    return ResponseEntity.noContent().build();
  }

  /**
   * Determina si el token JWT debe exponerse en el body de la respuesta.
   *
   * <p>
   * Devuelve {@code true} si el header {@code X-Client-Type} está presente
   * y su valor es {@code mobile} o {@code desktop}. En cualquier otro caso
   * (ausente, vacío, o {@code web}) devuelve {@code false}.
   */
  private boolean shouldIncludeTokenInBody(HttpServletRequest request) {
    String clientType = request.getHeader(CLIENT_TYPE_HEADER);
    return "mobile".equalsIgnoreCase(clientType)
        || "desktop".equalsIgnoreCase(clientType);
  }
}
