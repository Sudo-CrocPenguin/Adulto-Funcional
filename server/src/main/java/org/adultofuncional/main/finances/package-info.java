/**
 * Módulo financiero de la aplicación.
 *
 * <p>Gestiona movimientos financieros, gastos fijos y categorías
 * de clasificación bajo la arquitectura limpia.</p>
 *
 * <p>Entidades principales:
 * <ul>
 *   <li>{@code MovementEntity} — Ingresos y egresos</li>
 *   <li>{@code FixedExpensesEntity} — Gastos recurrentes (suscripciones, servicios)</li>
 *   <li>{@code CategoryEntity} — Clasificación (soporta soft delete)</li>
 * </ul>
 * </p>
 *
 * <p>Tablas asociadas: {@code movements}, {@code fixed_expenses},
 * {@code categories}. Todas usan UUID v7 como clave primaria.</p>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.finances;
