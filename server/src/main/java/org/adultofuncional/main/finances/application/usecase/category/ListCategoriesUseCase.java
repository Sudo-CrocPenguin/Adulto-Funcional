package org.adultofuncional.main.finances.application.usecase.category;

import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.adultofuncional.main.shared.pagination.PaginationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso: Listar categorías financieras con filtrado opcional por tipo.
 *
 * <p>
 * Recupera en SQL una página del catálogo SYSTEM y las categorías PERSONAL de
 * la cuenta, con filtro por módulo y búsqueda por nombre. El ownership forma
 * parte de la misma consulta que calcula los totales.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see CategoryRepository
 * @see CategoryFilterRequest
 */
@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

  private static final Set<String> ALLOWED_SORTS = Set.of("name", "type", "scope", "id");

  /**
   * Puerto de dominio para la consulta de categorías.
   */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el listado de categorías.
   *
   * @param filter Filtro opcional. Si {@code null} o su {@code type} es
   *               {@code null}, se retornan todas las categorías.
   * @return página de {@link CategoryResponse} y sus metadatos.
   */
  @Transactional(readOnly = true)
  public PageResult<CategoryResponse> execute(UUID accountId, CategoryFilterRequest filter) {
    PageQuery pageQuery = PaginationPolicy.resolve(
        filter == null ? null : filter.getPage(),
        filter == null ? null : filter.getSize(),
        filter == null ? null : filter.getSortBy(),
        filter == null ? null : filter.getSortDirection(),
        "name",
        true,
        ALLOWED_SORTS);
    PageResult<Category> categories = categoryRepository.findPageAccessible(
        accountId,
        filter == null ? null : filter.getType(),
        filter != null && StringUtils.hasText(filter.getSearchTerm())
            ? filter.getSearchTerm().trim()
            : null,
        pageQuery);
    return categories.map(c -> CategoryResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .type(c.getType())
            .scope(c.getScope())
            .build());
  }
}
