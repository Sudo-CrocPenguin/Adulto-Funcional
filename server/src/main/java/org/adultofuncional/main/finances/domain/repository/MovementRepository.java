package org.adultofuncional.main.finances.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.model.Movement;

/**
 * Puerto de dominio para la persistencia de movimientos financieros.
 *
 * <p>
 * Define las operaciones de acceso a datos que los casos de uso requieren
 * sobre la entidad {@link Movement}. La implementación concreta reside en
 * la capa de infraestructura (adaptador JPA) y se inyecta en tiempo de
 * ejecución, manteniendo el dominio desacoplado de los detalles de
 * almacenamiento.
 *
 * <p>
 * <strong>Operaciones expuestas:</strong>
 * <ul>
 * <li>Búsqueda individual limitada por ID y cuenta propietaria.</li>
 * <li>Listado de todos los movimientos asociados a una cuenta.</li>
 * <li>Persistencia de nuevos movimientos o actualización de existentes.</li>
 * <li>Eliminación limitada por ID y cuenta propietaria.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see Movement
 * @see org.adultofuncional.main.finances.infrastructure.repository.MovementRepositoryImpl
 */
public interface MovementRepository {

  /**
   * Busca un movimiento dentro de la cuenta propietaria indicada.
   *
   * @param id        UUID del movimiento
   * @param accountId UUID de la cuenta propietaria
   * @return movimiento cuando coinciden ID y cuenta; vacío en caso contrario
   */
  Optional<Movement> findByIdAndAccountId(UUID id, UUID accountId);

  /**
   * Lista todos los movimientos asociados a una cuenta específica.
   *
   * <p>
   * Utilizado por los casos de uso de listado y filtrado de movimientos.
   * Retorna la totalidad de los movimientos de la cuenta; el filtrado
   * adicional (por tipo, categoría, rango de fechas, término de búsqueda)
   * se aplica en memoria en la capa de aplicación.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de movimientos de la cuenta. Puede ser vacía si no hay
   *         registros.
   */
  List<Movement> findAllByAccountId(UUID accountId);

  /**
   * Persiste un movimiento nuevo o actualiza uno existente.
   *
   * <p>
   * Si el movimiento no tiene un ID asignado previamente, el repositorio lo
   * insertará como nuevo registro. Si ya existe, lo actualizará.
   *
   * @param movement el movimiento a guardar. No debe ser {@code null}.
   * @return el movimiento persistido con su estado final (incluyendo el ID
   *         si fue generado por la base de datos).
   */
  Movement save(Movement movement);

  /**
   * Elimina un movimiento únicamente cuando pertenece a la cuenta indicada.
   *
   * @param id        UUID del movimiento
   * @param accountId UUID de la cuenta propietaria
   * @return {@code true} cuando se eliminó una fila
   */
  boolean deleteByIdAndAccountId(UUID id, UUID accountId);
}
