package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Obtener una categoría por su identificador.
 *
 * <p>
 * Sirve como intermediario entre el controlador REST y el repositorio,
 * verificando que la categoría exista antes de retornar sus datos.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see CategoryRepository
 * @see Category
 */
@Service
@RequiredArgsConstructor
public class GetCategoryUseCase {

  /**
   * Puerto de dominio para la consulta de categorías.
   * Se resuelve en tiempo de ejecución con la implementación concreta
   * del módulo de infraestructura.
   */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la consulta de una categoría por su ID.
   *
   * @param categoryId Identificador único de la categoría. No puede ser
   *                   {@code null}.
   * @return {@link CategoryResponse} con los datos de la categoría.
   * @throws NotFoundException si no existe ninguna categoría con el ID
   *                           proporcionado.
   */
  @Transactional(readOnly = true)
  public CategoryResponse execute(UUID categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .build();
  }
}
