package org.adultofuncional.main.finances.domain.enums;

/**
 * Frecuencia de un gasto fijo recurrente.
 *
 * <p>
 * Define cada cuánto se repite el gasto. Los valores son los admitidos
 * en la columna {@code fixed_expense_frequency} de la tabla
 * {@code fixed_expenses}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
public enum Frequency {
  /** Una vez por semana. */
  SEMANAL,

  /** Cada dos semanas. */
  QUINCENAL,

  /** Una vez al mes. */
  MENSUAL,

  /** Una vez cada tres meses. */
  TRIMESTRAL,

  /** Una vez cada seis meses. */
  SEMESTRAL,

  /** Una vez al año. */
  ANUAL
}
