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


@DisplayName("AuthController - Tests unitarios")
public class AuthControllerTest {
    
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

            LoginRequest request = new LoginRequest();

            ResponseEntity<ApiResponse<AuthResponse>> result = controller.login(request);

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

            RegisterRequest request = new RegisterRequest();

            ResponseEntity<ApiResponse<AuthResponse>> result = controller.register(request);

            assertNotNull(result, "La respuesta no debe ser null");
            assertEquals(HttpStatus.CREATED, result.getStatusCode(), "register() debe retornar status 201");
        }
    }

}
