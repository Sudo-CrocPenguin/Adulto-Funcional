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
  WEEKLY,
  /** Cada dos semanas. */
  BIWEEKLY,
  /** Una vez al mes. */
  MONTHLY,
  /** Cada tres meses. */
  QUARTERLY,
  /** Cada seis meses. */
  SEMIANNUAL,
  /** Una vez al año. */
  ANNUAL
}
