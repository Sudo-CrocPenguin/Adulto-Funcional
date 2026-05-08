package org.adultofuncional.main.finances.domain.repository;

import org.adultofuncional.main.finances.domain.model.Category;
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
 * <strong>Operaciones expuestas:</strong>
 * <ul>
 * <li>Búsqueda individual por ID.</li>
 * <li>Listado completo.</li>
 * <li>Búsqueda por lote de IDs (para evitar consultas N+1 en listados que
 * requieren categorías asociadas).</li>
 * <li>Persistencia de nuevas categorías o actualización de existentes.</li>
 * <li>Eliminación por ID.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see Category
 * @see org.adultofuncional.main.finances.infrastructure.repository.CategoryRepositoryImpl
 */
public interface CategoryRepository {

  /**
   * Busca una categoría por su identificador único.
   *
   * @param id UUID de la categoría. No debe ser {@code null}.
   * @return {@link Optional} con la categoría si existe; {@code Optional.empty()}
   *         en caso contrario.
   */
  Optional<Category> findById(UUID id);

  /**
   * Retorna todas las categorías registradas en el sistema.
   *
   * @return lista de categorías. Puede ser vacía si no hay registros.
   */
  List<Category> findAll();

  /**
   * Busca múltiples categorías por sus identificadores en un solo lote.
   *
   * <p>
   * Utilizado por los casos de uso de listado de gastos fijos y movimientos
   * para cargar las categorías asociadas de forma eficiente, evitando
   * múltiples consultas individuales (problema N+1).
   *
   * @param ids colección de UUIDs de las categorías a buscar.
   * @return lista de categorías encontradas. Puede ser de menor tamaño que
   *         la entrada si algunos IDs no existen.
   */
  List<Category> findAllById(Iterable<UUID> ids);

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

  /**
   * Elimina una categoría por su identificador único.
   *
   * <p>
   * Si no existe una categoría con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso). La validación de existencia previa se realiza
   * en la capa de aplicación.
   *
   * @param id UUID de la categoría a eliminar. No debe ser {@code null}.
   */
  void deleteById(UUID id);
}
