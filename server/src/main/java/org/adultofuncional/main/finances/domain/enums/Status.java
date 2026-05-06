package org.adultofuncional.main.finances.domain.enums;

/**
 * Estado de un gasto fijo.
 *
 * <p>
 * Indica si el gasto fijo está actualmente activo (se tiene en cuenta
 * en los cálculos) o inactivo (pausado o cancelado).
 * Los valores coinciden con los permitidos en la columna
 * {@code fixed_expense_status} de la tabla {@code fixed_expenses}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
public enum Status {
  /** El gasto fijo se aplica normalmente. */
  ACTIVO,

  /** El gasto fijo está pausado o cancelado. */
  INACTIVO
}
