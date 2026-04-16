package org.adultofuncional.main.account.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para la entidad {@link AccountEntity}.
 * 
 * <p>
 * Verifica el comportamiento correcto de:
 * <ul>
 * <li>Callback de ciclo de vida JPA (@PrePersist)</li>
 * <li>Inicialización de colecciones para relaciones @OneToMany</li>
 * <li>Funcionamiento de getters y setters</li>
 * </ul>
 * 
 * @author juan
 * @since 0.0.1
 */
@DisplayName("AccountEntity - Tests de entidad JPA")
class AccountEntityTest {

  private AccountEntity account;

  @BeforeEach
  void setUp() {
    account = new AccountEntity();
  }

  /**
   * Tests relacionados con los callbacks del ciclo de vida JPA.
   */
  @Nested
  @DisplayName("Ciclo de vida JPA")
  class LifecycleCallbacks {

    /**
     * Verifica que el método anotado con {@code @PrePersist} establezca
     * automáticamente la fecha de creación antes de persistir la entidad.
     */
    @Test
    @DisplayName("Debe establecer account_created_at automáticamente en @PrePersist")
    void testPrePersistSetsCreatedAt() {
      // Given - Entidad recién creada sin fecha de creación

      // When - Se ejecuta el callback de pre-persistencia
      account.onCreate();

      // Then - La fecha de creación debe estar presente y ser válida
      assertNotNull(account.getAccount_created_at(),
          "La fecha de creación no debería ser null después de @PrePersist");
      assertTrue(account.getAccount_created_at() instanceof LocalDateTime,
          "La fecha de creación debe ser de tipo LocalDateTime");
    }
  }

  /**
   * Tests relacionados con la inicialización de colecciones para relaciones JPA.
   */
  @Nested
  @DisplayName("Inicialización de colecciones")
  class CollectionsInitialization {

    /**
     * Verifica que todas las colecciones para relaciones {@code @OneToMany}
     * estén correctamente inicializadas como ArrayList vacías.
     * 
     * <p>
     * Esto previene {@link NullPointerException} al agregar elementos
     * antes de que Hibernate proxy las colecciones.
     */
    @Test
    @DisplayName("Debe inicializar todas las colecciones como listas vacías")
    void testCollectionsInitialization() {
      // Given - Entidad recién instanciada

      // When - Se accede a las colecciones

      // Then - Todas las colecciones deben estar inicializadas y vacías
      assertNotNull(account.getMovements(),
          "La lista de movimientos no debería ser null");
      assertNotNull(account.getFixed_expenses(),
          "La lista de gastos fijos no debería ser null");
      assertNotNull(account.getEvents(),
          "La lista de eventos no debería ser null");
      assertNotNull(account.getPasswords(),
          "La lista de contraseñas no debería ser null");

      assertTrue(account.getMovements().isEmpty(),
          "La lista de movimientos debería estar vacía inicialmente");
      assertTrue(account.getFixed_expenses().isEmpty(),
          "La lista de gastos fijos debería estar vacía inicialmente");
      assertTrue(account.getEvents().isEmpty(),
          "La lista de eventos debería estar vacía inicialmente");
      assertTrue(account.getPasswords().isEmpty(),
          "La lista de contraseñas debería estar vacía inicialmente");
    }
  }

  /**
   * Tests relacionados con getters y setters de los campos básicos.
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
     * <li>account_id (UUID)</li>
     * <li>account_names (String)</li>
     * <li>account_lastnames (String)</li>
     * <li>account_email (String)</li>
     * <li>account_phone (String)</li>
     * <li>account_password (String)</li>
     * <li>account_master_key (String)</li>
     * <li>account_created_at (LocalDateTime)</li>
     * </ul>
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      // Given - Valores de prueba
      UUID id = UUID.randomUUID();
      LocalDateTime now = LocalDateTime.now();
      String nombres = "Juan";
      String apellidos = "Pérez";
      String email = "juan@email.com";
      String telefono = "123456789";
      String password = "encodedPass123";
      String masterKey = "masterKey123";

      // When - Se establecen todos los campos
      account.setAccount_id(id);
      account.setAccount_names(nombres);
      account.setAccount_lastnames(apellidos);
      account.setAccount_email(email);
      account.setAccount_phone(telefono);
      account.setAccount_password(password);
      account.setAccount_master_key(masterKey);
      account.setAccount_created_at(now);

      // Then - Los getters deben devolver exactamente los valores establecidos
      assertEquals(id, account.getAccount_id(),
          "El ID de cuenta debería coincidir");
      assertEquals(nombres, account.getAccount_names(),
          "Los nombres deberían coincidir");
      assertEquals(apellidos, account.getAccount_lastnames(),
          "Los apellidos deberían coincidir");
      assertEquals(email, account.getAccount_email(),
          "El email debería coincidir");
      assertEquals(telefono, account.getAccount_phone(),
          "El teléfono debería coincidir");
      assertEquals(password, account.getAccount_password(),
          "La contraseña debería coincidir");
      assertEquals(masterKey, account.getAccount_master_key(),
          "La master key debería coincidir");
      assertEquals(now, account.getAccount_created_at(),
          "La fecha de creación debería coincidir");
    }
  }
}
