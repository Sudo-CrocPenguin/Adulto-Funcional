package org.adultofuncional.main.finances.application.dto.category;

import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.OwnedResource;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta que expone los datos de una categoría financiera.
 *
 * <p>
 * Implementa {@link OwnedResource} para mantener la compatibilidad con
 * {@link org.adultofuncional.main.shared.security.OwnershipValidator},
 * aunque las categorías en el modelo actual no pertenecen a un usuario
 * específico. El método {@link #getEmail()} retorna {@code null} de forma
 * explícita, indicando que el recurso no tiene un propietario individual
 * verificable a través del validador de ownership.
 *
 * <p>
 * Nunca expone campos de infraestructura como marcas de borrado lógico
 * — la proyección se limita a los atributos necesarios para el cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see OwnedResource
 * @see org.adultofuncional.main.shared.security.OwnershipValidator
 * @see org.adultofuncional.main.finances.application.usecase.GetCategoryUseCase
 */
@Getter
@Builder
public class CategoryResponse implements OwnedResource {

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

  /**
   * Retorna el correo electrónico del propietario del recurso.
   *
   * <p>
   * Implementación del contrato definido por la interfaz {@link OwnedResource}
   * del módulo de seguridad compartido. En este DTO de respuesta de categoría,
   * retorna {@code null} dado que las categorías no están asociadas
   * directamente a un propietario identificable por correo electrónico
   * en la capa de presentación.
   *
   * @return {@code null} en todos los casos para este DTO.
   */
  @Override
  public String getEmail() {
    return null;
  }
}
