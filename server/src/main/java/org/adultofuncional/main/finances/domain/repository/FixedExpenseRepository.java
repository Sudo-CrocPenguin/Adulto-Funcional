package org.adultofuncional.main.finances.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.model.FixedExpense;

/**
 * Puerto de dominio para la persistencia de gastos fijos recurrentes.
 *
 * <p>
 * Define las operaciones de acceso a datos que los casos de uso requieren
 * sobre la entidad {@link FixedExpense}. La implementación concreta reside en
 * la capa de infraestructura (adaptador JPA) y se inyecta en tiempo de
 * ejecución, manteniendo el dominio desacoplado de los detalles de
 * almacenamiento.
 *
 * <p>
 * <strong>Operaciones expuestas:</strong>
 * <ul>
 * <li>Búsqueda individual limitada por ID y cuenta propietaria.</li>
 * <li>Listado de todos los gastos fijos asociados a una cuenta.</li>
 * <li>Persistencia de nuevos gastos fijos o actualización de existentes.</li>
 * <li>Eliminación limitada por ID y cuenta propietaria.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see FixedExpense
 * @see org.adultofuncional.main.finances.infrastructure.repository.FixedExpenseRepositoryImpl
 */
public interface FixedExpenseRepository {

  /**
   * Busca un gasto fijo dentro de la cuenta propietaria indicada.
   *
   * <p>
   * El filtro de cuenta forma parte de la consulta de persistencia. De este
   * modo un identificador ajeno produce el mismo resultado que uno inexistente
   * y el modelo de otra cuenta no llega a la capa de aplicación.
   *
   * @param id        UUID del gasto fijo. No debe ser {@code null}.
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return {@link Optional} con el gasto fijo cuando coincide ID y cuenta;
   *         {@code Optional.empty()} en caso contrario.
   */
  Optional<FixedExpense> findByIdAndAccountId(UUID id, UUID accountId);

  /**
   * Lista todos los gastos fijos asociados a una cuenta específica.
   *
   * <p>
   * Utilizado por los casos de uso de listado y filtrado de gastos fijos.
   * Retorna la totalidad de los gastos de la cuenta; el filtrado adicional
   * (por estado, categoría, término de búsqueda) se aplica en memoria en la
   * capa de aplicación.
   *
   * @param accountId UUID de la cuenta propietaria. No debe ser {@code null}.
   * @return lista de gastos fijos de la cuenta. Puede ser vacía si no hay
   *         registros.
   */
  List<FixedExpense> findAllByAccountId(UUID accountId);

  /**
   * Persiste un gasto fijo nuevo o actualiza uno existente.
   *
   * <p>
   * Si el gasto fijo no tiene un ID asignado previamente, el repositorio lo
   * insertará como nuevo registro. Si ya existe, lo actualizará.
   *
   * @param fixedExpense el gasto fijo a guardar. No debe ser {@code null}.
   * @return el gasto fijo persistido con su estado final.
   */
  FixedExpense save(FixedExpense fixedExpense);

  /**
   * Elimina un gasto fijo únicamente cuando pertenece a la cuenta indicada.
   *
   * @param id        UUID del gasto fijo a eliminar
   * @param accountId UUID de la cuenta propietaria
   * @return {@code true} cuando se eliminó una fila; {@code false} cuando el
   *         recurso no existe dentro de la cuenta
   */
  boolean deleteByIdAndAccountId(UUID id, UUID accountId);
}
