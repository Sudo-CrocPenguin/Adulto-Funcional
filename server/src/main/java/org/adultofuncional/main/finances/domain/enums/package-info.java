/**
 * Enumerados del dominio de finanzas.
 *
 * <p>
 * Define los valores constantes que tipifican los atributos principales de las
 * entidades del módulo financiero. Estos enumerados son utilizados tanto por la
 * capa de dominio como por los DTOs de aplicación y los filtros de consulta,
 * garantizando que los valores permitidos estén centralizados y sean
 * consistentes en todo el sistema.
 *
 * <h2>Enumerados incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.CategoryType} —
 * Ámbito de una categoría ({@code FINANCES} para movimientos y gastos fijos,
 * {@code AGENDA} para eventos del módulo de agenda).</li>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.Frequency} —
 * Periodicidad de un gasto fijo recurrente ({@code WEEKLY}, {@code BIWEEKLY},
 * {@code MONTHLY}, {@code QUARTERLY}, {@code SEMIANNUAL}, {@code ANNUAL}).</li>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.MovementType} —
 * Naturaleza de un movimiento financiero ({@code INCOME} para ingresos,
 * {@code EXPENSE} para egresos).</li>
 * <li>{@link org.adultofuncional.main.finances.domain.enums.Status} —
 * Estado operativo de un gasto fijo ({@code ACTIVE} o {@code INACTIVE}).</li>
 * </ul>
 *
 * <h2>Uso en el sistema</h2>
 * <ul>
 * <li>Las entidades de dominio
 * {@link org.adultofuncional.main.finances.domain.model.Category},
 * {@link org.adultofuncional.main.finances.domain.model.FixedExpense} y
 * {@link org.adultofuncional.main.finances.domain.model.Movement} referencian
 * estos enumerados para sus atributos tipificados.</li>
 * <li>Los DTOs de entrada y salida de la capa de aplicación exponen estos
 * valores para comunicación con los clientes de la API.</li>
 * <li>Los filtros de consulta ({@code *FilterRequest}) permiten acotar
 * resultados utilizando estos valores.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.model
 * @see org.adultofuncional.main.finances.application.dto
 */
package org.adultofuncional.main.finances.domain.enums;
