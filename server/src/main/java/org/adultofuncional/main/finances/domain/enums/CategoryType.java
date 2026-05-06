package org.adultofuncional.main.finances.domain.enums;

/**
 * Ámbito al que pertenece una categoría.
 *
 * <p>
 * Las categorías pueden clasificar elementos del módulo financiero
 * ({@code FINANCES}) o del módulo de agenda ({@code AGENDA}).
 * Estos valores corresponden a los admitidos en la columna
 * {@code category_type} de la tabla {@code categories}.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
public enum CategoryType {
  /** Categoría usada en movimientos y gastos fijos. */
  FINANCES,

  /** Categoría usada en eventos de agenda. */
  AGENDA
}
