/**
 * Casos de uso del módulo de finanzas para la gestión de gastos fijos
 * recurrentes.
 *
 * <p>
 * Orquesta las operaciones de creación, consulta, actualización, eliminación
 * y listado de gastos fijos asociados a una cuenta. Cada caso de uso coordina
 * el dominio
 * ({@link org.adultofuncional.main.finances.domain.model.FixedExpense})
 * y los puertos de repositorio correspondientes, validando reglas de negocio
 * como la existencia de la cuenta, la categoría, fechas futuras y la
 * pertenencia
 * del gasto a la cuenta indicada.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.fixedexpense.CreateFixedExpenseUseCase}
 * —
 * Crea un nuevo gasto fijo recurrente. Valida que la cuenta y categoría
 * existan,
 * y que la fecha de cierre sea futura.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.fixedexpense.GetFixedExpenseUseCase}
 * —
 * Obtiene un gasto fijo por su UUID junto con la categoría asociada
 * anidada.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.fixedexpense.UpdateFixedExpenseUseCase}
 * —
 * Actualiza parcialmente un gasto fijo. Aplica cambios solo a los campos
 * proporcionados y gestiona el estado (activar/desactivar).</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.fixedexpense.DeleteFixedExpenseUseCase}
 * —
 * Elimina un gasto fijo por su identificador, verificando su existencia.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.fixedexpense.ListFixedExpensesUseCase}
 * —
 * Lista todos los gastos fijos de una cuenta con filtros opcionales por
 * estado, categoría y término de búsqueda. Carga las categorías en lote
 * para evitar consultas N+1.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Todas las operaciones de escritura ({@code create}, {@code update},
 * {@code delete}) se ejecutan con {@code @Transactional} para garantizar
 * atomicidad.</li>
 * <li>La validación de propiedad del gasto fijo (pertenencia a la cuenta)
 * se aplica en los casos de uso que modifican o consultan un gasto
 * específico.</li>
 * <li>La carga de categorías se realiza a través del puerto
 * {@link org.adultofuncional.main.finances.domain.repository.CategoryRepository},
 * y la existencia de la cuenta se verifica con el puerto
 * {@link org.adultofuncional.main.account.domain.repository.AccountRepository}
 * del módulo de cuentas.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.dto.fixedexpense
 * @see org.adultofuncional.main.finances.domain.model.FixedExpense
 * @see org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository
 */
package org.adultofuncional.main.finances.application.usecase.fixedexpense;
