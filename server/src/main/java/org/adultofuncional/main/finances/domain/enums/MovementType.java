package org.adultofuncional.main.finances.domain.enums;

/**
 * Tipo de movimiento financiero.
 *
 * <p>
 * Define si un movimiento es un ingreso (suma) o un egreso (resta).
 * Los valores coinciden con los permitidos en la columna
 * {@code movement_type} de la tabla {@code movements}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
public enum MovementType {
  /** Entrada de dinero. */
  INGRESO,

  /** Salida de dinero. */
  EGRESO
}
