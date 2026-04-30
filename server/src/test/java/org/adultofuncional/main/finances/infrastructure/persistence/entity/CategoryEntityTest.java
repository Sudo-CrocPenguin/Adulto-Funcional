package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la entidad JPA {@link CategoryEntity}.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>Callback {@code @PrePersist} establece {@code category_created_at}</li>
 *   <li>Soft delete mediante {@code softDelete()} y {@code @SQLRestriction}</li>
 *   <li>Colecciones {@code @OneToMany} inicializadas como listas vacías</li>
 *   <li>Getters y setters de todos los campos</li>
 *   <li>Tipos de categoría: {@code "Finanzas"} y {@code "Agenda"}</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@DisplayName("CategoryEntity")
class CategoryEntityTest {

  private CategoryEntity category;

  @BeforeEach
  void setUp() {
    category = new CategoryEntity();
  }

  @Nested
  @DisplayName("Ciclo de vida JPA")
  class LifecycleCallbacks {

    /**
     * Verifica que {@code onCreate()} establece {@code category_created_at}.
     */
    @Test
    @DisplayName("Debe establecer category_created_at en @PrePersist")
    void testPrePersistSetsCreatedAt() {
      category.onCreate();
      assertNotNull(category.getCategory_created_at());
      assertTrue(category.getCategory_created_at() instanceof LocalDateTime);
    }
  }

  @Nested
  @DisplayName("Soft Delete")
  class SoftDelete {

    /**
     * Verifica que {@code softDelete()} establece {@code category_deleted_at}.
     */
    @Test
    @DisplayName("Debe establecer category_deleted_at al ejecutar softDelete()")
    void testSoftDelete() {
      assertNull(category.getCategory_deleted_at());

      category.softDelete();

      assertNotNull(category.getCategory_deleted_at());
    }

    /**
     * Verifica que {@code softDelete()} puede llamarse múltiples veces.
     */
    @Test
    @DisplayName("Debe permitir múltiples llamadas a softDelete()")
    void testSoftDeleteIsIdempotent() {
      category.softDelete();
      LocalDateTime firstDelete = category.getCategory_deleted_at();

      category.softDelete();
      LocalDateTime secondDelete = category.getCategory_deleted_at();

      assertNotNull(firstDelete);
      assertNotNull(secondDelete);
    }
  }

  @Nested
  @DisplayName("Inicialización de colecciones")
  class CollectionsInitialization {

    /**
     * Verifica que {@code movements}, {@code fixed_expenses} y {@code events}
     * están inicializados como listas vacías.
     */
    @Test
    @DisplayName("Debe inicializar todas las colecciones como listas vacías")
    void testCollectionsInitialization() {
      assertNotNull(category.getMovements());
      assertNotNull(category.getFixed_expenses());
      assertNotNull(category.getEvents());

      assertTrue(category.getMovements().isEmpty());
      assertTrue(category.getFixed_expenses().isEmpty());
      assertTrue(category.getEvents().isEmpty());
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
      String nombre = "Alimentos";
      String tipo = "Finanzas";

      category.setCategory_id(id);
      category.setCategory_name(nombre);
      category.setCategory_type(tipo);
      category.setCategory_created_at(now);
      category.setCategory_deleted_at(now);

      assertEquals(id, category.getCategory_id());
      assertEquals(nombre, category.getCategory_name());
      assertEquals(tipo, category.getCategory_type());
      assertEquals(now, category.getCategory_created_at());
      assertEquals(now, category.getCategory_deleted_at());
    }

    /**
     * Verifica que se aceptan los tipos {@code "Finanzas"} y {@code "Agenda"}.
     */
    @Test
    @DisplayName("Debe aceptar tipos 'Finanzas' y 'Agenda'")
    void testCategoryTypeValues() {
      category.setCategory_type("Finanzas");
      assertEquals("Finanzas", category.getCategory_type());

      category.setCategory_type("Agenda");
      assertEquals("Agenda", category.getCategory_type());
    }
  }
}
