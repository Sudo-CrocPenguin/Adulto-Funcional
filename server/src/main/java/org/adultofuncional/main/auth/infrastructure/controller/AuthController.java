package org.adultofuncional.main.auth.infrastructure.controller;

import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.usecase.LoginUseCase;
import org.adultofuncional.main.auth.application.usecase.RegisterUseCase;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  /**
   * Endpoint para iniciar sesión en la aplicación.
   *
   * <p>
   * Recibe las credenciales del usuario, las valida y delega al
   * {@link LoginUseCase}. Si la autenticación es exitosa, retorna
   * un token JWT junto con los datos de la cuenta.
   *
   * @param request objeto con email y contraseña del usuario
   * @return 200 OK con token y datos de la cuenta
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request) {

    AuthResponse response = loginUseCase.execute(request);

    return ResponseEntity.ok(
        ApiResponse.<AuthResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Inicio de sesión exitoso")
            .data(response)
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
   * @param request objeto con los datos del nuevo usuario
   * @return 201 CREATED con token y datos de la cuenta recién creada
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {

    AuthResponse response = registerUseCase.execute(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
            ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Cuenta creada exitosamente")
                .data(response)
                .build());
  }
}
