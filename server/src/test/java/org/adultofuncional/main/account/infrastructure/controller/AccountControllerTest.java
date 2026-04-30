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
 * Pruebas unitarias del controlador {@link AccountController}.
 *
 * <p>Verifica los status HTTP del esqueleto actual:
 * <ul>
 *   <li>{@code GET /api/account/{id}} → 200 OK</li>
 *   <li>{@code PUT /api/account/{id}} → 200 OK</li>
 *   <li>{@code DELETE /api/account/{id}} → 204 No Content</li>
 * </ul>
 *
 * @author Lydis Jaraba
 * @since 0.0.1
 * @see AccountController
 */
@DisplayName("AccountController")
class AccountControllerTest {

    // TODO: ampliar tests cuando los use cases estén disponibles
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
            UUID id = UUID.randomUUID();
            ResponseEntity<Object> result = controller.getAccount(id);
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
        }
    }

    @Nested
    @DisplayName("updateAccount()")
    class UpdateAccount {

        @Test
        @DisplayName("Debe retornar status 200")
        void testUpdateAccountReturnsStatus200() {
            UUID id = UUID.randomUUID();
            Object request = new Object();
            ResponseEntity<Object> result = controller.updateAccount(id, request);
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
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
            assertNotNull(result);
            assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        }
    }
}
