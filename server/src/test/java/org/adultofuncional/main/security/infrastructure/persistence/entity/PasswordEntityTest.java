package org.adultofuncional.main.security.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la entidad JPA {@link PasswordEntity}.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>Getters y setters de todos los campos</li>
 *   <li>Campo opcional {@code password_last_change_date} (puede ser null)</li>
 *   <li>Restricciones de longitud: {@code password_application_name} VARCHAR(35),
 *       {@code password_application} TEXT</li>
 * </ul>
 *
 * <p>Seguridad: en producción {@code password_application} almacena la contraseña
 * encriptada con AES-256 usando la Master Key del usuario.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@DisplayName("PasswordEntity")
class PasswordEntityTest {

  private PasswordEntity password;

  @BeforeEach
  void setUp() {
    password = new PasswordEntity();
  }

  @Nested
  @DisplayName("Getters y Setters")
  class GettersAndSetters {

    /**
     * Establece y recupera todos los campos de la entidad.
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      UUID id = UUID.randomUUID();
      String applicationName = "Gmail";
      String applicationPassword = "miPassword123";
      LocalDate lastChange = LocalDate.now();

      password.setPassword_id(id);
      password.setPassword_application_name(applicationName);
      password.setPassword_application(applicationPassword);
      password.setPassword_last_change_date(lastChange);

      assertEquals(id, password.getPassword_id());
      assertEquals(applicationName, password.getPassword_application_name());
      assertEquals(applicationPassword, password.getPassword_application());
      assertEquals(lastChange, password.getPassword_last_change_date());
    }

    /**
     * Verifica que el constructor sin argumentos crea una instancia no null.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      assertNotNull(password);
    }
  }

  @Nested
  @DisplayName("Fecha de último cambio (campo opcional)")
  class LastChangeDate {

    /**
     * Verifica que {@code password_last_change_date} puede ser null.
     */
    @Test
    @DisplayName("Debe permitir que la fecha de último cambio sea null")
    void testLastChangeDateCanBeNull() {
      password.setPassword_last_change_date(LocalDate.now());
      assertNotNull(password.getPassword_last_change_date());

      password.setPassword_last_change_date(null);
      assertNull(password.getPassword_last_change_date());
    }

    /**
     * Verifica que la fecha de último cambio es null por defecto.
     */
    @Test
    @DisplayName("Debe tener fecha de último cambio null al crear la entidad")
    void testLastChangeDateIsNullByDefault() {
      assertNull(password.getPassword_last_change_date());
    }

    /**
     * Verifica que se aceptan fechas futuras (programación de rotación).
     */
    @Test
    @DisplayName("Debe aceptar fechas futuras")
    void testFutureLastChangeDate() {
      LocalDate futureDate = LocalDate.now().plusMonths(3);
      password.setPassword_last_change_date(futureDate);
      assertEquals(futureDate, password.getPassword_last_change_date());
    }

    /**
     * Verifica que se aceptan fechas pasadas (registro histórico).
     */
    @Test
    @DisplayName("Debe aceptar fechas pasadas")
    void testPastLastChangeDate() {
      LocalDate pastDate = LocalDate.now().minusMonths(6);
      password.setPassword_last_change_date(pastDate);
      assertEquals(pastDate, password.getPassword_last_change_date());
    }
  }

  @Nested
  @DisplayName("Restricciones de longitud")
  class LengthConstraints {

    /**
     * Verifica que se aceptan valores en el límite de longitud de las columnas:
     * {@code password_application_name} VARCHAR(35), {@code password_application} TEXT.
     */
    @Test
    @DisplayName("Debe respetar límites de longitud de VARCHAR en BD")
    void testLengthConstraints() {
      String maxName = "A".repeat(35);
      String maxPassword = "B".repeat(20);

      password.setPassword_application_name(maxName);
      password.setPassword_application(maxPassword);

      assertEquals(35, password.getPassword_application_name().length());
      assertEquals(20, password.getPassword_application().length());
    }
  }

  @Nested
  @DisplayName("Propósito de la entidad")
  class EntityPurpose {

    /**
     * Verifica que la entidad puede representar credenciales de diferentes servicios.
     */
    @Test
    @DisplayName("Debe poder almacenar credenciales de diferentes servicios")
    void testDifferentServiceCredentials() {
      String[][] services = {
          { "Gmail", "pass123" },
          { "Netflix", "netflix456" },
          { "Banco", "bankPass789" },
          { "GitHub", "ghToken000" }
      };

      for (String[] service : services) {
        password.setPassword_application_name(service[0]);
        password.setPassword_application(service[1]);

        assertEquals(service[0], password.getPassword_application_name());
        assertEquals(service[1], password.getPassword_application());
      }
    }
  }
}
