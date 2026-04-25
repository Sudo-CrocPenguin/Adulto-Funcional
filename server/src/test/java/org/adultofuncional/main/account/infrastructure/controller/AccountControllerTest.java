package org.adultofuncional.main.account.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * Tests unitarios para {@link AccountController}.
 *
 * <p>Verifica el comportamiento actual del esqueleto:
 * <ul>
 *   <li>{@code getAccount()} retorna status 200</li>
 *   <li>{@code updateAccount()} retorna status 200</li>
 *   <li>{@code deleteAccount()} retorna status 204</li>
 * </ul>
 *
 * <p><strong>⚠️ Pendiente:</strong> ampliar tests cuando
 * los use cases estén disponibles.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see AccountController
 */

@DisplayName("AccountController - Tests unitarios")
class AccountControllerTest {

    private AccountController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountController();

    }

    @Nested
    @DisplayName("getAccount()")
    class GetAccount {

        @Test
        @DisplayName("Debe retornar status 200")
        void testGetAccountReturnsStatus200() {

            //Given - un UUID cualquiera
            UUID id = UUID.randomUUID();

            //When - se llama al endpoint
            ResponseEntity<Object> result = controller.getAccount(id);

            //Then - debe retornar status 200
            assertNotNull(result, "La respuesta no debe ser null");
            assertEquals(HttpStatus.OK, result.getStatusCode(), "getAccount() debe retornar status 200");
        }
    }

    @Nested
    @DisplayName("updateAccount()")
    class UpdateAccount {

        @Test
        @DisplayName("Debe retornar status 200")
        void testUpdateAccountReturnsStatus200() {

            //Given - un UUID y un request cualquiera
            UUID id = UUID.randomUUID();
            Object request = new Object();

            ResponseEntity<Object> result = controller.updateAccount(id, request);

            assertNotNull(result, "La respuesta no debe ser null");
            assertEquals(HttpStatus.OK, result.getStatusCode(), "updateAccount() debe retornar status 200");
        }
    }

    @Nested
    @DisplayName("deleteAccount()")
    class DeleteAccount {

        @Test
        @DisplayName("Debe retornar status 204")
        void testDeleteAccountReturnsStatus204() {

            UUID id = UUID.randomUUID();

            ResponseEntity<Void> result = controller.deleteAccount(id);

            assertNotNull(result, "La respuesta no debe ser null");
            assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(), "deleteAccount() debe retornar status 204");
        }
    }
}