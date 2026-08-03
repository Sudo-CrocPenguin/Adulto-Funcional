package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.adultofuncional.main.shared.pagination.PaginationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Listar los gastos fijos de una cuenta aplicando filtros
 * opcionales y retornando la categoría asociada a cada uno.
 *
 * <p>
 * Ejecuta ownership, filtros, orden y límites en SQL. La capa de aplicación
 * transforma únicamente la página solicitada y carga sus categorías
 * accesibles en un lote.
 *
 * <p>
 * <strong>Filtros soportados (todos opcionales):</strong>
 * <ul>
 * <li>{@code status} — filtra por estado ({@code ACTIVE},
 * {@code PAUSED}…).</li>
 * <li>{@code categoryId} — filtra por categoría asociada.</li>
 * <li>{@code searchTerm} — búsqueda insensible a mayúsculas sobre el
 * nombre.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see FixedExpenseRepository
 * @see CategoryRepository
 * @see AccountRepository
 */
@Service
@RequiredArgsConstructor
public class ListFixedExpensesUseCase {

  private static final Set<String> ALLOWED_SORTS =
      Set.of("nextDueDate", "amount", "name", "status", "frequency", "id");

  /** Puerto de dominio para la consulta de gastos fijos. */
  private final FixedExpenseRepository fixedExpenseRepository;

  /** Puerto de dominio para la validación de la cuenta (módulo account). */
  private final AccountRepository accountRepository;

  /** Puerto de dominio para la carga en lote de categorías. */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el listado filtrado de gastos fijos.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param filter    Filtro opcional con estado, categoría y término de
   *                  búsqueda. Puede ser {@code null} para obtener todos
   *                  los gastos fijos de la cuenta.
   * @return página de {@link FixedExpenseResponse} con categoría y metadatos.
   * @throws NotFoundException si la cuenta no existe.
   */
  @Transactional(readOnly = true)
  public PageResult<FixedExpenseResponse> execute(UUID accountId, FixedExpenseFilterRequest filter) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    PageQuery pageQuery = PaginationPolicy.resolve(
        filter == null ? null : filter.getPage(),
        filter == null ? null : filter.getSize(),
        filter == null ? null : filter.getSortBy(),
        filter == null ? null : filter.getSortDirection(),
        "nextDueDate",
        true,
        ALLOWED_SORTS);
    PageResult<FixedExpense> expenses = fixedExpenseRepository.findPageByAccountId(
        accountId,
        filter == null ? null : filter.getStatus(),
        filter == null ? null : filter.getCategoryId(),
        filter != null && StringUtils.hasText(filter.getSearchTerm())
            ? filter.getSearchTerm().trim()
            : null,
        pageQuery);

    // Carga en lote de categorías para evitar N+1
    Set<UUID> categoryIds = expenses.content().stream()
        .map(FixedExpense::getCategoryId)
        .collect(Collectors.toSet());
    Map<UUID, Category> categoryMap = categoryRepository
        .findAllAccessibleById(accountId, categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));

    return expenses.map(e -> {
          Category cat = categoryMap.get(e.getCategoryId());
          CategoryResponse catResp = cat != null ? CategoryResponse.builder()
              .id(cat.getId())
              .name(cat.getName())
              .type(cat.getType())
              .scope(cat.getScope())
              .build() : null;

          return FixedExpenseResponse.builder()
              .id(e.getId())
              .name(e.getName())
              .frequency(e.getFrequency())
              .amount(e.getAmount())
              .status(e.getStatus())
              .nextDueDate(e.getNextDueDate())
              .category(catResp)
              .build();
        });
  }
}
