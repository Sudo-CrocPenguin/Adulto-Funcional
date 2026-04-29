package org.adultofuncional.main.auth.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.LoginRequest;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.shared.response.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Tests unitarios para {@link AuthController}.
 * 
 * <p>Verifica el comportamiento actual del esqueleto:
 * <ul>
 *   <li>{@code login()} retorna status 200</li>
 *   <li>{@code register()} retorna status 201</li>
 * </ul>
 *
 * <p><b>Nota:</b> estos tests serán ampliados cuando {@code LoginUseCase}
 * y {@code RegisterUseCase} estén disponibles.</p>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see AuthController
 */

@DisplayName("AuthController - Tests unitarios")
public class AuthControllerTest {
    
    // TODO: ampliar tests cuando LoginUseCase y RegisterUseCase estén disponibles
    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(null, null);
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Debe retornar status 200")
        void testLoginReturnsStatus200() {

             // Given - un request de login cualquiera
            LoginRequest request = new LoginRequest();
        
            // When - se llama al endpoint
            ResponseEntity<ApiResponse<AuthResponse>> result = controller.login(request);

            // Then - debe retornar status 200
            // TODO: verificar token y datos de cuenta cuando LoginUseCase esté implementado
            assertNotNull(result, "La respuesta no debe ser null");
            assertEquals(HttpStatus.OK, result.getStatusCode(), "Login() debe retornar status 200");
        }
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Debe retornar status 201")
        void testRegisterReturnsStatus201() {

            // Given - un request de registro cualquiera
            RegisterRequest request = new RegisterRequest();

            // When - se llama al endpoint
            ResponseEntity<ApiResponse<AuthResponse>> result = controller.register(request);

            // Then - debe retornar status 201
            // TODO: verificar token y datos de cuenta cuando RegisterUseCase esté implementado
            assertNotNull(result, "La respuesta no debe ser null");
            assertEquals(HttpStatus.CREATED, result.getStatusCode(), "register() debe retornar status 201");
        }
    }

}
