package org.adultofuncional.main.finances.application.usecase.category;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Listar categorías financieras con filtrado opcional por tipo.
 *
 * <p>
 * Recupera todas las categorías desde el repositorio y aplica un filtro en
 * memoria si se especifica un {@link CategoryFilterRequest} con un tipo
 * concreto. Retorna la lista de {@link CategoryResponse} correspondiente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see CategoryRepository
 * @see CategoryFilterRequest
 */
@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

  /**
   * Puerto de dominio para la consulta de categorías.
   */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el listado de categorías.
   *
   * @param filter Filtro opcional. Si {@code null} o su {@code type} es
   *               {@code null}, se retornan todas las categorías.
   * @return Lista de {@link CategoryResponse} (vacía si no hay coincidencias).
   */
  @Transactional(readOnly = true)
  public List<CategoryResponse> execute(CategoryFilterRequest filter) {
    List<Category> categories = categoryRepository.findAll();
    if (filter != null && filter.getType() != null) {
      categories = categories.stream()
          .filter(c -> c.getType() == filter.getType())
          .collect(Collectors.toList());
    }
    return categories.stream()
        .map(c -> CategoryResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .type(c.getType())
            .build())
        .collect(Collectors.toList());
  }
}
