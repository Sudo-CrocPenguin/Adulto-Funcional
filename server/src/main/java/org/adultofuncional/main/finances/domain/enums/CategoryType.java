package org.adultofuncional.main.finances.domain.enums;

/**
 * Define el ámbito de aplicación de una categoría.
 *
 * <p>
 * Las categorías pueden servir para clasificar elementos del módulo de
 * finanzas (movimientos, gastos fijos) o del módulo de agenda (eventos).
 * El valor de este enumerado se almacena en la columna {@code category_type}
 * de la tabla {@code categories} y se utiliza como criterio de filtro en
 * {@link org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest}
 * y en la creación de categorías.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.domain.model.Category
 * @see org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest
 */
public enum CategoryType {
  /**
   * Categoría destinada a movimientos y gastos fijos del módulo financiero.
   */
  FINANCES,

  /**
   * Categoría destinada a eventos del módulo de agenda.
   */
  AGENDA
}
