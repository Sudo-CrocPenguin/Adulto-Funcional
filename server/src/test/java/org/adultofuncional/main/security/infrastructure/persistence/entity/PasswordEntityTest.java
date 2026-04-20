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
 * Tests unitarios para la entidad {@link PasswordEntity}.
 * 
 * <p>
 * Verifica el comportamiento correcto de:
 * <ul>
 * <li>Funcionamiento de getters y setters para todos los campos</li>
 * <li>Manejo de campos opcionales (fecha de último cambio)</li>
 * <li>Validación de estructura para almacenamiento de credenciales</li>
 * </ul>
 * 
 * <p>
 * <strong>Características destacadas:</strong>
 * <ul>
 * <li><b>Almacenamiento de credenciales:</b> Guarda nombres de aplicación y sus
 * contraseñas asociadas</li>
 * <li><b>Fecha de último cambio opcional:</b> Permite auditoría de rotación de
 * contraseñas</li>
 * <li><b>Relación con cuenta:</b> Cada contraseña pertenece a una cuenta de
 * usuario ({@code @ManyToOne})</li>
 * </ul>
 * 
 * <p>
 * <strong>⚠️ Consideración de seguridad importante:</strong>
 * <br>
 * Esta entidad almacena contraseñas en texto plano en la base de datos.
 * En una implementación real, estos valores DEBEN ser encriptados usando
 * la master key del usuario o un mecanismo de cifrado simétrico (AES-256).
 * 
 * <p>
 * Esta entidad forma parte del módulo de seguridad y permite a los usuarios
 * almacenar de forma centralizada sus credenciales de acceso a diferentes
 * servicios.
 * 
 * @author juan
 * @since 0.0.1
 */
@DisplayName("PasswordEntity - Tests de entidad JPA para almacenamiento de credenciales")
class PasswordEntityTest {

  private PasswordEntity password;

  @BeforeEach
  void setUp() {
    password = new PasswordEntity();
  }

  /**
   * Tests relacionados con getters y setters de todos los campos.
   */
  @Nested
  @DisplayName("Getters y Setters")
  class GettersAndSetters {

    /**
     * Verifica que todos los getters y setters de la entidad funcionen
     * correctamente,
     * estableciendo y recuperando valores para cada campo.
     * 
     * <p>
     * Campos probados:
     * <ul>
     * <li>password_id (UUID) - Identificador único</li>
     * <li>password_application_name (String) - Nombre del servicio/aplicación (máx.
     * 35 chars)</li>
     * <li>password_application (String) - Contraseña almacenada (máx. 20
     * chars)</li>
     * <li>password_last_change_date (LocalDate) - Fecha de último cambio
     * (opcional)</li>
     * </ul>
     * 
     * <p>
     * <strong>Nota sobre relaciones:</strong> El campo {@code account}
     * ({@code @ManyToOne}) se prueba en tests de integración con base de datos.
     * 
     * <p>
     * <strong>Nota de seguridad:</strong> El campo {@code password_application}
     * está limitado a 20 caracteres. En producción, este campo debería almacenar
     * la contraseña encriptada, no el valor en texto plano.
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      // Given - Valores de prueba representativos
      UUID id = UUID.randomUUID();
      String applicationName = "Gmail";
      String applicationPassword = "miPassword123";
      LocalDate lastChange = LocalDate.now();

      // When - Se establecen todos los campos
      password.setPassword_id(id);
      password.setPassword_application_name(applicationName);
      password.setPassword_application(applicationPassword);
      password.setPassword_last_change_date(lastChange);

      // Then - Los getters deben devolver exactamente los valores establecidos
      assertEquals(id, password.getPassword_id(),
          "El ID de la contraseña debería coincidir");
      assertEquals(applicationName, password.getPassword_application_name(),
          "El nombre de la aplicación debería coincidir");
      assertEquals(applicationPassword, password.getPassword_application(),
          "La contraseña almacenada debería coincidir");
      assertEquals(lastChange, password.getPassword_last_change_date(),
          "La fecha de último cambio debería coincidir");
    }

    /**
     * Verifica que el constructor sin argumentos cree una instancia válida.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con el constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      // Given - setUp ya creó la instancia

      // Then - La entidad debe existir
      assertNotNull(password,
          "El constructor sin argumentos debería crear una instancia válida");
    }
  }

  /**
   * Tests relacionados con la fecha de último cambio (campo opcional).
   */
  @Nested
  @DisplayName("Fecha de último cambio (campo opcional)")
  class LastChangeDate {

    /**
     * Verifica que el campo {@code password_last_change_date} pueda ser null.
     * 
     * <p>
     * Este campo es opcional porque:
     * <ul>
     * <li>Al crear una nueva contraseña, puede no tener fecha de cambio previa</li>
     * <li>No todas las contraseñas requieren seguimiento de rotación</li>
     * <li>Es un campo de auditoría, no obligatorio para el funcionamiento</li>
     * </ul>
     * 
     * <p>
     * En la base de datos, la columna está definida sin restricción
     * {@code NOT NULL}, permitiendo valores nulos.
     */
    @Test
    @DisplayName("Debe permitir que la fecha de último cambio sea null")
    void testLastChangeDateCanBeNull() {
      // Given - Entidad con fecha de cambio establecida
      password.setPassword_last_change_date(LocalDate.now());
      assertNotNull(password.getPassword_last_change_date(),
          "Inicialmente la fecha no debería ser null");

      // When - Se establece explícitamente a null
      password.setPassword_last_change_date(null);

      // Then - Debe aceptar el valor null sin problemas
      assertNull(password.getPassword_last_change_date(),
          "La fecha de último cambio debería poder ser null");
    }

    /**
     * Verifica que al crear una nueva entidad, la fecha de último cambio
     * sea inicialmente null (no se inicializa automáticamente).
     */
    @Test
    @DisplayName("Debe tener fecha de último cambio null al crear la entidad")
    void testLastChangeDateIsNullByDefault() {
      // Given - Entidad recién creada en setUp

      // Then - La fecha debe ser null por defecto
      assertNull(password.getPassword_last_change_date(),
          "La fecha de último cambio debería ser null al crear la entidad");
    }

    /**
     * Verifica que se pueda establecer una fecha de cambio futura.
     * Útil para programar recordatorios de rotación de contraseñas.
     */
    @Test
    @DisplayName("Debe aceptar fechas de último cambio futuras")
    void testFutureLastChangeDate() {
      // Given - Fecha futura (ej. para programar próximo cambio)
      LocalDate futureDate = LocalDate.now().plusMonths(3);

      // When - Se asigna fecha futura
      password.setPassword_last_change_date(futureDate);

      // Then - Debe aceptarse sin restricciones
      assertEquals(futureDate, password.getPassword_last_change_date(),
          "La fecha de último cambio futura debería ser aceptada");
    }

    /**
     * Verifica que se pueda establecer una fecha de cambio pasada.
     * Útil para registrar cambios históricos.
     */
    @Test
    @DisplayName("Debe aceptar fechas de último cambio pasadas")
    void testPastLastChangeDate() {
      // Given - Fecha pasada (ej. último cambio realizado hace tiempo)
      LocalDate pastDate = LocalDate.now().minusMonths(6);

      // When - Se asigna fecha pasada
      password.setPassword_last_change_date(pastDate);

      // Then - Debe aceptarse sin restricciones
      assertEquals(pastDate, password.getPassword_last_change_date(),
          "La fecha de último cambio pasada debería ser aceptada");
    }
  }

  /**
   * Tests relacionados con validaciones de longitud de campos.
   */
  @Nested
  @DisplayName("Restricciones de longitud")
  class LengthConstraints {

    /**
     * Verifica las longitudes máximas definidas en la base de datos.
     * 
     * <p>
     * Restricciones:
     * <ul>
     * <li>password_application_name: VARCHAR(35)</li>
     * <li>password_application: VARCHAR(20)</li>
     * </ul>
     * 
     * <p>
     * Estas restricciones son importantes porque:
     * <ul>
     * <li>La contraseña encriptada podría exceder el límite original</li>
     * <li>Algunos nombres de aplicación pueden ser largos</li>
     * </ul>
     */
    @Test
    @DisplayName("Debe respetar límites de longitud de VARCHAR en BD")
    void testLengthConstraints() {
      // Given - Valores que cumplen con los límites máximos
      String maxName = "A".repeat(35); // Exactamente 35 caracteres
      String maxPassword = "B".repeat(20); // Exactamente 20 caracteres

      // When - Se asignan valores en el límite
      password.setPassword_application_name(maxName);
      password.setPassword_application(maxPassword);

      // Then - Deben ser aceptados (la validación real ocurre en BD)
      assertEquals(35, password.getPassword_application_name().length(),
          "El nombre de aplicación debe aceptar hasta 35 caracteres");
      assertEquals(20, password.getPassword_application().length(),
          "La contraseña debe aceptar hasta 20 caracteres");
      assertEquals(maxName, password.getPassword_application_name(),
          "El nombre de aplicación máximo debería ser aceptado");
      assertEquals(maxPassword, password.getPassword_application(),
          "La contraseña máxima debería ser aceptada");
    }

    /**
     * Nota sobre la limitación de 20 caracteres para contraseñas.
     * 
     * <p>
     * <strong>⚠️ Advertencia de diseño:</strong>
     * Una contraseña encriptada (ej. con AES-256 + Base64) puede exceder
     * fácilmente los 20 caracteres. Si se implementa encriptación en el futuro,
     * este campo DEBERÁ ser ampliado a VARCHAR(255) o TEXT.
     */
    @Test
    @DisplayName("Debe documentar limitación de 20 caracteres para contraseñas encriptadas")
    void testPasswordLengthLimitationWarning() {
      // Given - Una contraseña encriptada típica (Base64)
      // Ejemplo: "a7F3kL9mN2xP5qR8sT1vW4yZ6==" son 28 caracteres

      // When - Se intenta asignar una contraseña "encriptada" larga
      String encryptedPassword = "a7F3kL9mN2xP5qR8sT1vW4yZ6==="; // 28 chars > 20

      // Then - A nivel de entidad Java se acepta, pero fallará en BD
      password.setPassword_application(encryptedPassword);
      assertEquals(28, password.getPassword_application().length(),
          "Java acepta la contraseña larga, pero la BD la rechazará");

      // Este test sirve como documentación de una limitación de diseño
    }
  }

  /**
   * Tests relacionados con el propósito de la entidad.
   */
  @Nested
  @DisplayName("Propósito de la entidad")
  class EntityPurpose {

    /**
     * Verifica que la entidad pueda representar diferentes tipos de credenciales.
     */
    @Test
    @DisplayName("Debe poder almacenar credenciales de diferentes servicios")
    void testDifferentServiceCredentials() {
      // Given - Diferentes servicios con sus credenciales
      String[][] services = {
          { "Gmail", "pass123" },
          { "Netflix", "netflix456" },
          { "Banco", "bankPass789" },
          { "GitHub", "ghToken000" }
      };

      // When & Then - Debe aceptar cada servicio
      for (String[] service : services) {
        password.setPassword_application_name(service[0]);
        password.setPassword_application(service[1]);

        assertEquals(service[0], password.getPassword_application_name(),
            "Debería aceptar el nombre del servicio: " + service[0]);
        assertEquals(service[1], password.getPassword_application(),
            "Debería aceptar la credencial para: " + service[0]);
      }
    }
  }
}
