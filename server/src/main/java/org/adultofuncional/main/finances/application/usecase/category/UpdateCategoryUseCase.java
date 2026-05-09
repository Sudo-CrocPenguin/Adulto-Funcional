package org.adultofuncional.main.finances.application.usecase.category;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso: Actualizar los datos de una categoría existente.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>La categoría debe existir.</li>
 * <li>Solo se actualizan los campos proporcionados (nombre y tipo).</li>
 * <li>El nombre solo se actualiza si tiene texto significativo
 * (verificado con {@link StringUtils#hasText}).</li>
 * <li>El tipo se actualiza si se proporciona un valor no nulo.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see CategoryRepository
 * @see Category
 */
@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la actualización de los datos de una categoría.
   *
   * @param categoryId Identificador de la categoría a modificar.
   * @param request    DTO con los nuevos valores. Los campos nulos o sin texto
   *                   son ignorados.
   * @return {@link CategoryResponse} con los datos actualizados.
   * @throws NotFoundException Si no existe una categoría con el ID
   *                           proporcionado.
   */
  @Transactional
  public CategoryResponse execute(UUID categoryId, UpdateCategoryRequest request) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));

    if (StringUtils.hasText(request.getName())) {
      category.updateName(request.getName());
    }
    if (request.getType() != null) {
      category.updateType(request.getType());
    }

    Category saved = categoryRepository.save(category);
    return CategoryResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .type(saved.getType())
        .build();
  }
}
