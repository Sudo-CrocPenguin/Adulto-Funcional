package org.adultofuncional.main.finances.application.dto.category;

import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.enums.CategoryScope;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta que expone los datos de una categoría financiera.
 *
 * <p>
 * Distingue categorías inmutables del catálogo global y categorías personales
 * administradas exclusivamente por su cuenta propietaria.
 *
 * <p>
 * Nunca expone campos de infraestructura como marcas de borrado lógico
 * — la proyección se limita a los atributos necesarios para el cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.shared.security.OwnershipValidator
 * @see org.adultofuncional.main.finances.application.usecase.category.GetCategoryUseCase
 */
@Getter
@Builder
public class CategoryResponse {

  /**
   * Identificador único de la categoría.
   *
   * <p>
   * Corresponde al UUID generado por el sistema al momento de crear
   * la categoría. Permite identificarla de forma unívoca en todas
   * las operaciones del sistema.
   */
  private UUID id;

  /**
   * Nombre descriptivo de la categoría.
   *
   * <p>
   * Representa la etiqueta legible por el usuario que identifica
   * la categoría dentro del sistema financiero (por ejemplo:
   * "Alimentación", "Transporte", "Salario").
   */
  private String name;

  /**
   * Tipo de la categoría según la clasificación del dominio financiero.
   *
   * <p>
   * Corresponde a un valor del enumerado {@link CategoryType}, que
   * determina la naturaleza de la categoría dentro del sistema
   * (por ejemplo: ingreso, gasto, ahorro, entre otros).
   */
  private CategoryType type;

  /** Indica si la categoría pertenece al sistema o a la cuenta autenticada. */
  private CategoryScope scope;

}
