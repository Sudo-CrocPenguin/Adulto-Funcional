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
 * <li>Búsqueda individual por ID.</li>
 * <li>Listado de todos los gastos fijos asociados a una cuenta.</li>
 * <li>Persistencia de nuevos gastos fijos o actualización de existentes.</li>
 * <li>Eliminación por ID.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see FixedExpense
 * @see org.adultofuncional.main.finances.infrastructure.repository.FixedExpenseRepositoryImpl
 */
public interface FixedExpenseRepository {

  /**
   * Busca un gasto fijo por su identificador único.
   *
   * @param id UUID del gasto fijo. No debe ser {@code null}.
   * @return {@link Optional} con el gasto fijo si existe;
   *         {@code Optional.empty()} en caso contrario.
   */
  Optional<FixedExpense> findById(UUID id);

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
   * Elimina un gasto fijo por su identificador único.
   *
   * <p>
   * Si no existe un gasto fijo con el ID dado, la operación no tiene efecto
   * (comportamiento silencioso). La validación de existencia previa se
   * realiza en la capa de aplicación.
   *
   * @param id UUID del gasto fijo a eliminar. No debe ser {@code null}.
   */
  void deleteById(UUID id);
}
