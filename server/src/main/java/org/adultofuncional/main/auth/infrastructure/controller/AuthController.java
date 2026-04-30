package org.adultofuncional.main.auth.infrastructure.controller;

import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.auth.application.usecase.LoginUseCase;
import org.adultofuncional.main.auth.application.usecase.RegisterUseCase;

import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Controlador REST encargado de gestionar las operaciones de autenticación.
 *
 * <p>Expone los endpoints públicos de la API relacionados con el acceso al sistema,
 * como el inicio de sesión y el registro de nuevos usuarios. Actúa como punto de
 * entrada de las peticiones HTTP y delega la lógica de negocio a los casos de uso
 * correspondientes.</p>
 *
 * <p>Todas las rutas de este controlador están disponibles bajo el prefijo
 * {@code /api/auth}.</p>
 * 
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    /** Caso de uso encargado de la lógica de inicio de sesión. */
    private final LoginUseCase loginUseCase;

    /** Caso de uso encargado de la lógica de registro de nuevos usuarios. */
    private final RegisterUseCase registerUseCase;

    /**
     * Constructor que inyecta los casos de uso necesarios para la autenticación.
     *
     * <p>Spring inyecta automáticamente las dependencias al momento de crear
     * el controlador.</p>
     *
     * @param loginUseCase    caso de uso para iniciar sesión
     * @param registerUseCase caso de uso para registrar un nuevo usuario
     */

    public AuthController(LoginUseCase loginUseCase, RegisterUseCase registerUseCase) {

        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
    }

    /**
     * Endpoint para iniciar sesión en la aplicación.
     *
     * <p>Recibe las credenciales del usuario (correo y contraseña), las valida
     * y las delega al {@link LoginUseCase} para autenticar al usuario. Si la
     * autenticación es exitosa, retorna un token de acceso junto con la
     * información de la cuenta.</p>
     *
     * @param request objeto con las credenciales del usuario (correo y contraseña)
     * @return respuesta con el token de autenticación y datos de la cuenta,
     *         envueltos en un {@link ApiResponse}
     */

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        //TODO: implementar cuando LoginUseCase esté listo

        return null;
    }

    /**
     * Endpoint para registrar un nuevo usuario en la aplicación.
     *
     * <p>Recibe los datos del formulario de registro (nombres, apellidos, teléfono,
     * correo y contraseña), los valida y los delega al {@link RegisterUseCase}
     * para crear la cuenta. Si el correo ya está registrado, se lanzará una
     * excepción de conflicto.</p>
     *
     * @param request objeto con los datos del nuevo usuario
     * @return respuesta con el token de autenticación y datos de la cuenta recién
     *         creada, envueltos en un {@link ApiResponse}
     */

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        //TODO: implementar cuando RegisterUseCase esté listo 

        return null;
    }
}
