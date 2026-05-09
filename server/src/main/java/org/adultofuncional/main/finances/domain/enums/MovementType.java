package org.adultofuncional.main.finances.domain.enums;

/**
 * Tipo de movimiento financiero.
 *
 * <p>
 * Determina si un movimiento representa una entrada de dinero ({@link #INCOME})
 * o una salida ({@link #EXPENSE}). El valor se almacena en la columna
 * {@code movement_type} de la tabla {@code movements} y se expone en los DTOs
 * {@link org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest},
 * {@link org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest}
 * y
 * {@link org.adultofuncional.main.finances.application.dto.movement.MovementResponse}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.model.Movement
 */
public enum MovementType {
  /** Entrada de dinero (suma al balance). */
  INCOME,

  /** Salida de dinero (resta del balance). */
  EXPENSE
}
