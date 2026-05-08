package org.adultofuncional.main.finances.domain.enums;

/**
 * Estado operativo de un gasto fijo.
 *
 * <p>
 * Indica si el gasto fijo se encuentra activo y se tiene en cuenta en los
 * cálculos financieros ({@link #ACTIVE}) o si está pausado o cancelado
 * ({@link #INACTIVE}). El valor se almacena en la columna
 * {@code fixed_expense_status} de la tabla {@code fixed_expenses} y se utiliza
 * en los DTOs de creación, actualización, filtro y respuesta de gastos fijos
 * ({@link org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest},
 * {@link org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest},
 * {@link org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest},
 * {@link org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse}).
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.model.FixedExpense
 */
public enum Status {
  /** El gasto fijo está activo y se considera en los cálculos recurrentes. */
  ACTIVE,

  /** El gasto fijo está pausado o cancelado, excluyéndose de los cálculos. */
  INACTIVE
}
