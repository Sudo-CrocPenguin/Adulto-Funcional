/**
 * Casos de uso del módulo de finanzas para la gestión de movimientos
 * financieros.
 *
 * <p>
 * Orquesta las operaciones de creación, consulta, actualización y listado
 * de movimientos (ingresos y egresos) asociados a una cuenta. Cada caso de uso
 * coordina el dominio
 * ({@link org.adultofuncional.main.finances.domain.model.Movement})
 * y los puertos de repositorio correspondientes, aplicando reglas de validación
 * como la existencia de la cuenta, la pertenencia del movimiento a la cuenta
 * y la integridad de las categorías asociadas.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.movement.CreateMovementUseCase}
 * —
 * Registra un nuevo movimiento. Valida que la cuenta exista y, si se
 * especifica,
 * que la categoría exista.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.movement.GetMovementUseCase}
 * —
 * Obtiene un movimiento por su UUID. Verifica la propiedad del movimiento
 * (pertenencia a la cuenta) antes de retornar los datos.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.movement.UpdateMovementUseCase}
 * —
 * Actualiza parcialmente un movimiento. Aplica solo los campos proporcionados
 * y valida la existencia de la nueva categoría si se modifica.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.movement.ListMovementsUseCase}
 * —
 * Lista los movimientos de una cuenta con filtros opcionales por tipo,
 * categoría, rango de fechas y término de búsqueda. El filtrado se realiza
 * en memoria sobre el conjunto completo de movimientos de la cuenta.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Todas las operaciones de escritura ({@code create}, {@code update})
 * se ejecutan con {@code @Transactional} para garantizar atomicidad.</li>
 * <li>La propiedad del movimiento se verifica en {@code GetMovementUseCase}
 * y {@code UpdateMovementUseCase} comparando el {@code accountId} de la
 * entidad con el recibido como parámetro. Si no coinciden, se lanza
 * {@link org.adultofuncional.main.shared.exception.NotFoundException}
 * para no revelar la existencia del recurso.</li>
 * <li>Actualmente ninguna respuesta de movimiento incluye la categoría
 * anidada (se deja como {@code null} a la espera de una implementación
 * futura).</li>
 * <li>El filtrado en {@code ListMovementsUseCase} podría delegarse a la
 * base de datos (Query Methods / Criteria API) para mejorar el rendimiento
 * con grandes volúmenes de datos, en lugar de aplicar filtros en memoria.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.dto.movement
 * @see org.adultofuncional.main.finances.domain.model.Movement
 * @see org.adultofuncional.main.finances.domain.repository.MovementRepository
 */
package org.adultofuncional.main.finances.application.usecase.movement;
