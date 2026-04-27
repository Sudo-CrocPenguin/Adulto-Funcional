package org.adultofuncional.main.account.application.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.adultofuncional.main.account.domain.model.Account;

/**
 * ============================================================
 * AccountTest
 * ============================================================
 *
 * ¿Qué es?
 * Clase de pruebas unitarias para el modelo de dominio Account.
 *
 * ¿Para qué sirve?
 * Permite validar que la lógica de negocio definida en la clase Account
 * funcione correctamente, asegurando la integridad de los datos y el
 * comportamiento esperado del sistema.
 *
 * ¿Qué hace?
 * - Verifica la correcta creación de una cuenta
 * - Valida reglas de negocio como nombre vacío y email inválido
 * - Comprueba el comportamiento del dominio:
 *   activar, desactivar y actualizar datos
 *
 * ============================================================
 */
public class AccountTest {

    @Test
    void shouldCreateAccountSuccessfully() {
        // Arrange
        String name = "Juan";
        String email = "juan@test.com";
        String phone = "123";

        // Act
        Account account = Account.create(name, email, phone);

        // Assert
        assertNotNull(account.getId());
        assertEquals(name, account.getName());
        assertEquals(email, account.getEmail());
        assertTrue(account.isActive());
    }

    @Test
    void shouldFailWhenNameIsEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Account.create("", "test@test.com", "123");
        });

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Account.create("Juan", "correo-invalido", "123");
        });

        assertEquals("Invalid email", exception.getMessage());
    }

    @Test
    void shouldDeactivateAccount() {
        // Arrange
        Account account = Account.create("Juan", "juan@test.com", "123");

        // Act
        account.deactivate();

        // Assert
        assertFalse(account.isActive());
    }

    @Test
    void shouldActivateAccount() {
        // Arrange
        Account account = Account.create("Juan", "juan@test.com", "123");

        // Act
        account.deactivate();
        account.activate();

        // Assert
        assertTrue(account.isActive());
    }

    @Test
    void shouldUpdateAccountDetails() {
        // Arrange
        Account account = Account.create("Juan", "juan@test.com", "123");

        // Act
        account.updateDetails("Pedro", "999");

        // Assert
        assertEquals("Pedro", account.getName());
        assertEquals("999", account.getPhone());
    }
}