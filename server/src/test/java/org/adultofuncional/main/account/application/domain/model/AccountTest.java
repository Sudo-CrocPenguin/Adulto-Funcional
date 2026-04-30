package org.adultofuncional.main.account.application.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.adultofuncional.main.account.domain.model.Account;

/**
 * Pruebas unitarias del modelo de dominio {@link Account}.
 *
 * <p>
 * Verifica las reglas de negocio y validaciones del agregado Account:
 * <ul>
 * <li>Creación válida con factory method {@code create()}</li>
 * <li>Rechazo de nombre vacío o email con formato inválido</li>
 * <li>Activación y desactivación de la cuenta</li>
 * <li>Actualización de nombre y teléfono</li>
 * </ul>
 *
 * @author Jeronimo Ospina
 * @since 0.0.1
 * @see Account
 */
public class AccountTest {

  /**
   * Crea una cuenta con datos válidos y verifica que el UUID no es null,
   * los campos coinciden y la cuenta está activa.
   *
   * @see Account#create(String, String, String)
   */
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

  /**
   * Verifica que se lanza {@link IllegalArgumentException} con mensaje
   * {@code "Name cannot be empty"} al intentar crear una cuenta con nombre vacío.
   *
   * @see Account#create(String, String, String)
   */
  @Test
  void shouldFailWhenNameIsEmpty() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      Account.create("", "test@test.com", "123");
    });

    assertEquals("Name cannot be empty", exception.getMessage());
  }

  /**
   * Verifica que se lanza {@link IllegalArgumentException} con mensaje
   * {@code "Invalid email"} al intentar crear una cuenta con email sin formato
   * válido.
   *
   * @see Account#create(String, String, String)
   */
  @Test
  void shouldFailWhenEmailIsInvalid() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      Account.create("Juan", "correo-invalido", "123");
    });

    assertEquals("Invalid email", exception.getMessage());
  }

  /**
   * Crea una cuenta, la desactiva y verifica que {@code isActive()} retorna
   * false.
   *
   * @see Account#deactivate()
   */
  @Test
  void shouldDeactivateAccount() {
    // Arrange
    Account account = Account.create("Juan", "juan@test.com", "123");

    // Act
    account.deactivate();

    // Assert
    assertFalse(account.isActive());
  }

  /**
   * Crea una cuenta, la desactiva y luego la reactiva. Verifica que
   * {@code isActive()} retorna true después de {@code activate()}.
   *
   * @see Account#activate()
   */
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

  /**
   * Actualiza nombre y teléfono de una cuenta existente y verifica
   * que los campos se modifican correctamente mientras el email permanece igual.
   *
   * @see Account#updateDetails(String, String)
   */
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
