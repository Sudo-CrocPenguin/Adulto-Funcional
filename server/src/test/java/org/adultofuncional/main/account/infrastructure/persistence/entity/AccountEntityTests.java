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
 * Pruebas unitarias de la entidad JPA {@link AccountEntity}.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>Callback {@code @PrePersist} establece {@code account_created_at}</li>
 *   <li>Colecciones {@code @OneToMany} inicializadas como listas vacías</li>
 *   <li>Getters y setters de todos los campos</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@DisplayName("AccountEntity")
class AccountEntityTest {

  private AccountEntity account;

  @BeforeEach
  void setUp() {
    account = new AccountEntity();
  }

  @Nested
  @DisplayName("Ciclo de vida JPA")
  class LifecycleCallbacks {

    /**
     * Verifica que {@code onCreate()} establece {@code account_created_at}
     * con la fecha y hora actual.
     */
    @Test
    @DisplayName("Debe establecer account_created_at en @PrePersist")
    void testPrePersistSetsCreatedAt() {
      account.onCreate();

      assertNotNull(account.getAccount_created_at());
      assertTrue(account.getAccount_created_at() instanceof LocalDateTime);
    }
  }

  @Nested
  @DisplayName("Inicialización de colecciones")
  class CollectionsInitialization {

    /**
     * Verifica que las colecciones {@code movements}, {@code fixed_expenses},
     * {@code events} y {@code passwords} no son null y están vacías.
     */
    @Test
    @DisplayName("Debe inicializar todas las colecciones como listas vacías")
    void testCollectionsInitialization() {
      assertNotNull(account.getMovements());
      assertNotNull(account.getFixed_expenses());
      assertNotNull(account.getEvents());
      assertNotNull(account.getPasswords());

      assertTrue(account.getMovements().isEmpty());
      assertTrue(account.getFixed_expenses().isEmpty());
      assertTrue(account.getEvents().isEmpty());
      assertTrue(account.getPasswords().isEmpty());
    }
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
      LocalDateTime now = LocalDateTime.now();
      String nombres = "Juan";
      String apellidos = "Pérez";
      String email = "juan@email.com";
      String telefono = "123456789";
      String password = "encodedPass123";
      String masterKey = "masterKey123";

      account.setAccount_id(id);
      account.setAccount_names(nombres);
      account.setAccount_lastnames(apellidos);
      account.setAccount_email(email);
      account.setAccount_phone(telefono);
      account.setAccount_password(password);
      account.setAccount_master_key(masterKey);
      account.setAccount_created_at(now);

      assertEquals(id, account.getAccount_id());
      assertEquals(nombres, account.getAccount_names());
      assertEquals(apellidos, account.getAccount_lastnames());
      assertEquals(email, account.getAccount_email());
      assertEquals(telefono, account.getAccount_phone());
      assertEquals(password, account.getAccount_password());
      assertEquals(masterKey, account.getAccount_master_key());
      assertEquals(now, account.getAccount_created_at());
    }
  }
}
