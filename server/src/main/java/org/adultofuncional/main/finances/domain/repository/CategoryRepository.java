package org.adultofuncional.main.finances.domain.repository;

import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de dominio para la persistencia de categorías.
 *
 * <p>
 * Define las operaciones de acceso a datos que los casos de uso requieren
 * sobre la entidad {@link Category}. La implementación concreta reside en la
 * capa de infraestructura (adaptador JPA) y se inyecta en tiempo de ejecución,
 * manteniendo el dominio desacoplado de los detalles de almacenamiento.
 *
 * <p>
 * Todas las lecturas públicas reciben una cuenta para impedir que la capa de
 * aplicación omita accidentalmente el alcance SYSTEM/PERSONAL.
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see Category
 * @see org.adultofuncional.main.finances.infrastructure.repository.CategoryRepositoryImpl
 */
public interface CategoryRepository {

  /** Busca una categoría SYSTEM o una PERSONAL de la cuenta indicada. */
  Optional<Category> findAccessibleById(UUID accountId, UUID categoryId);

  /** Valida ownership y tipo de módulo dentro de una única consulta. */
  Optional<Category> findAccessibleByIdAndType(
      UUID accountId,
      UUID categoryId,
      CategoryType type);

  /** Busca exclusivamente una categoría PERSONAL de su propietario. */
  Optional<Category> findPersonalByIdAndOwner(UUID accountId, UUID categoryId);

  /** Lista el catálogo SYSTEM y las categorías PERSONAL de la cuenta. */
  List<Category> findAllAccessible(UUID accountId, CategoryType type);

  /** Recupera por lote únicamente categorías visibles para la cuenta. */
  List<Category> findAllAccessibleById(UUID accountId, Iterable<UUID> ids);

  /** Elimina atómicamente una categoría PERSONAL de la cuenta. */
  boolean deletePersonalByIdAndOwner(UUID accountId, UUID categoryId);

  /**
   * Persiste una categoría nueva o actualiza una existente.
   *
   * <p>
   * Si la categoría no tiene un ID asignado previamente, el repositorio la
   * insertará como nuevo registro. Si ya existe, la actualizará.
   *
   * @param category la categoría a guardar. No debe ser {@code null}.
   * @return la categoría persistida con su estado final (incluyendo el ID
   *         si fue generado por la base de datos).
   */
  Category save(Category category);

}
