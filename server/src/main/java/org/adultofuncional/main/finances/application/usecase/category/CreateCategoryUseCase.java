package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Crear una nueva categoría financiera.
 *
 * <p>
 * Orquesta la construcción del modelo de dominio {@link Category} a partir
 * de los datos validados, la persiste mediante el puerto
 * {@link CategoryRepository} y retorna la proyección {@link CategoryResponse}
 * con los datos asignados por el sistema (UUID, fecha de creación).
 *
 * <p>
 * La construcción del {@link CategoryResponse} se realiza de forma manual
 * en este caso de uso, sin delegar en un mapper externo, ya que la proyección
 * es directa y no involucra transformaciones complejas.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see Category
 * @see CategoryRepository
 * @see CreateCategoryRequest
 * @see CategoryResponse
 */
@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

  /**
   * Puerto de dominio para la persistencia de categorías.
   * Se resuelve en tiempo de ejecución con la implementación concreta
   * del módulo de infraestructura.
   */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la creación de una nueva categoría.
   *
   * @param request DTO con el nombre y tipo de la categoría, ya validados
   *                por el framework (Jakarta Validation + {@code @NoHtml}).
   * @return {@link CategoryResponse} con los datos de la categoría creada,
   *         incluyendo el identificador UUID asignado.
   */
  @Transactional
  public CategoryResponse execute(CreateCategoryRequest request) {
    Category category = Category.create(request.getName(), request.getType());
    Category saved = categoryRepository.save(category);
    return CategoryResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .type(saved.getType())
        .build();
  }
}
