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
 * Tests unitarios para la entidad {@link CategoryEntity}.
 * 
 * <p>
 * Verifica el comportamiento correcto de:
 * <ul>
 * <li>Callback de ciclo de vida JPA (@PrePersist) para fecha de creación</li>
 * <li>Mecanismo de borrado lógico (soft delete) mediante fecha de
 * eliminación</li>
 * <li>Inicialización de colecciones para relaciones @OneToMany</li>
 * <li>Funcionamiento de getters y setters</li>
 * </ul>
 * 
 * <p>
 * <strong>Característica destacada: Soft Delete</strong>
 * <br>
 * Esta entidad implementa borrado lógico mediante el campo
 * {@code category_deleted_at}
 * y la anotación {@code @SQLRestriction("category_deleted_at IS NULL")} que
 * filtra
 * automáticamente las categorías eliminadas en todas las consultas JPA.
 * 
 * <p>
 * Las categorías pueden ser de tipo:
 * <ul>
 * <li>Ingreso - Para movimientos de entrada de dinero</li>
 * <li>Gasto - Para movimientos de salida de dinero</li>
 * </ul>
 * 
 * @author juan
 * @since 0.0.1
 * @see org.hibernate.annotations.SQLRestriction
 */
@DisplayName("CategoryEntity - Tests de entidad JPA para categorías con soft delete")
class CategoryEntityTest {

  private CategoryEntity category;

  @BeforeEach
  void setUp() {
    category = new CategoryEntity();
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
     * 
     * <p>
     * Este timestamp permite auditoría y ordenamiento por fecha de creación.
     */
    @Test
    @DisplayName("Debe establecer category_created_at automáticamente en @PrePersist")
    void testPrePersistSetsCreatedAt() {
      // Given - Entidad recién creada sin fecha de creación

      // When - Se ejecuta el callback de pre-persistencia
      category.onCreate();

      // Then - La fecha de creación debe estar presente
      assertNotNull(category.getCategory_created_at(),
          "La fecha de creación no debería ser null después de @PrePersist");
      assertTrue(category.getCategory_created_at() instanceof LocalDateTime,
          "La fecha de creación debe ser de tipo LocalDateTime");
    }
  }

  /**
   * Tests relacionados con el mecanismo de borrado lógico (soft delete).
   */
  @Nested
  @DisplayName("Soft Delete - Borrado lógico")
  class SoftDelete {

    /**
     * Verifica que el método {@code softDelete()} establezca correctamente
     * la fecha de eliminación, marcando la categoría como "borrada" sin
     * eliminarla físicamente de la base de datos.
     * 
     * <p>
     * <strong>Importante:</strong> La anotación {@code @SQLRestriction}
     * en la entidad filtrará automáticamente las categorías con
     * {@code deleted_at != null} en todas las consultas JPA.
     * 
     * <p>
     * Este comportamiento permite:
     * <ul>
     * <li>Mantener integridad referencial con movimientos históricos</li>
     * <li>Recuperar categorías "borradas" si es necesario</li>
     * <li>Auditar cuándo se eliminó una categoría</li>
     * </ul>
     */
    @Test
    @DisplayName("Debe establecer category_deleted_at al ejecutar softDelete()")
    void testSoftDelete() {
      // Given - Categoría recién creada (no eliminada)
      assertNull(category.getCategory_deleted_at(),
          "Inicialmente, la fecha de eliminación debería ser null");

      // When - Se ejecuta el borrado lógico
      category.softDelete();

      // Then - La fecha de eliminación debe estar presente
      assertNotNull(category.getCategory_deleted_at(),
          "Después de softDelete(), la fecha de eliminación no debería ser null");
      assertTrue(category.getCategory_deleted_at() instanceof LocalDateTime,
          "La fecha de eliminación debe ser de tipo LocalDateTime");
    }

    /**
     * Verifica que {@code softDelete()} pueda llamarse múltiples veces
     * sin efectos secundarios inesperados (idempotencia).
     */
    @Test
    @DisplayName("Debe permitir múltiples llamadas a softDelete() sin errores")
    void testSoftDeleteIsIdempotent() {
      // Given - Primera llamada a softDelete
      category.softDelete();
      LocalDateTime firstDelete = category.getCategory_deleted_at();

      // When - Segunda llamada (podría pasar en flujos repetidos)
      category.softDelete();
      LocalDateTime secondDelete = category.getCategory_deleted_at();

      // Then - Ambas fechas deben existir (aunque la segunda sobrescribe la primera)
      assertNotNull(firstDelete,
          "La primera fecha de eliminación debería existir");
      assertNotNull(secondDelete,
          "La segunda fecha de eliminación debería existir");
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
     * Relaciones verificadas:
     * <ul>
     * <li>movements - Movimientos asociados a esta categoría</li>
     * <li>fixed_expenses - Gastos fijos de esta categoría</li>
     * <li>events - Eventos de agenda con esta categoría</li>
     * </ul>
     * 
     * <p>
     * Esto previene {@link NullPointerException} al intentar agregar
     * elementos antes de que Hibernate inicialice los proxies.
     */
    @Test
    @DisplayName("Debe inicializar todas las colecciones como listas vacías")
    void testCollectionsInitialization() {
      // Given - Entidad recién instanciada

      // When - Se accede a las colecciones

      // Then - Todas las colecciones deben estar inicializadas y vacías
      assertNotNull(category.getMovements(),
          "La lista de movimientos no debería ser null");
      assertNotNull(category.getFixed_expenses(),
          "La lista de gastos fijos no debería ser null");
      assertNotNull(category.getEvents(),
          "La lista de eventos no debería ser null");

      assertTrue(category.getMovements().isEmpty(),
          "La lista de movimientos debería estar vacía inicialmente");
      assertTrue(category.getFixed_expenses().isEmpty(),
          "La lista de gastos fijos debería estar vacía inicialmente");
      assertTrue(category.getEvents().isEmpty(),
          "La lista de eventos debería estar vacía inicialmente");
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
     * <li>category_id (UUID) - Identificador único</li>
     * <li>category_name (String) - Nombre de la categoría (máx. 20 chars)</li>
     * <li>category_type (String) - Tipo: "Ingreso" o "Gasto"</li>
     * <li>category_created_at (LocalDateTime) - Fecha de creación</li>
     * <li>category_deleted_at (LocalDateTime) - Fecha de borrado lógico</li>
     * </ul>
     * 
     * <p>
     * <strong>Nota sobre category_type:</strong> En una versión futura,
     * este campo podría convertirse en un Enum para mayor type safety.
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      // Given - Valores de prueba
      UUID id = UUID.randomUUID();
      LocalDateTime now = LocalDateTime.now();
      String nombre = "Alimentos";
      String tipo = "Gasto";

      // When - Se establecen todos los campos
      category.setCategory_id(id);
      category.setCategory_name(nombre);
      category.setCategory_type(tipo);
      category.setCategory_created_at(now);
      category.setCategory_deleted_at(now);

      // Then - Los getters deben devolver exactamente los valores establecidos
      assertEquals(id, category.getCategory_id(),
          "El ID de categoría debería coincidir");
      assertEquals(nombre, category.getCategory_name(),
          "El nombre de la categoría debería coincidir");
      assertEquals(tipo, category.getCategory_type(),
          "El tipo de categoría debería coincidir");
      assertEquals(now, category.getCategory_created_at(),
          "La fecha de creación debería coincidir");
      assertEquals(now, category.getCategory_deleted_at(),
          "La fecha de eliminación debería coincidir");
    }

    /**
     * Verifica que los tipos de categoría aceptados sean los esperados.
     * Aunque no hay validación a nivel de entidad, documenta los valores válidos.
     */
    @Test
    @DisplayName("Debe aceptar los tipos de categoría 'Ingreso' y 'Gasto'")
    void testCategoryTypeValues() {
      // Given - Valores válidos según el dominio
      String tipoIngreso = "Ingreso";
      String tipoGasto = "Gasto";

      // When & Then - Debe aceptar "Ingreso"
      category.setCategory_type(tipoIngreso);
      assertEquals(tipoIngreso, category.getCategory_type());

      // When & Then - Debe aceptar "Gasto"
      category.setCategory_type(tipoGasto);
      assertEquals(tipoGasto, category.getCategory_type());
    }
  }
}
