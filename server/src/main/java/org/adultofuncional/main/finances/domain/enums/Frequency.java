package org.adultofuncional.main.finances.domain.enums;

/**
 * Frecuencia de recurrencia de un gasto fijo.
 *
 * <p>
 * Define cada cuánto tiempo se repite un gasto fijo. El valor se almacena
 * en la columna {@code fixed_expense_frequency} de la tabla
 * {@code fixed_expenses} y se utiliza en los DTOs de creación y actualización
 * de gastos fijos
 * ({@link org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest},
 * {@link org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest})
 * y en la proyección
 * {@link org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.model.FixedExpense
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
