package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.List;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Listar los gastos fijos de una cuenta aplicando filtros
 * opcionales y retornando la categoría asociada a cada uno.
 *
 * <p>
 * Recupera todos los gastos fijos de la cuenta desde
 * {@link FixedExpenseRepository#findAllByAccountId} y aplica en memoria
 * los filtros proporcionados: estado, categoría y término de búsqueda
 * sobre el nombre. Para evitar el problema N+1, las categorías de los
 * gastos filtrados se cargan en un único lote a través de
 * {@link CategoryRepository#findAllById}.
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
   * @return Lista de {@link FixedExpenseResponse} con la categoría anidada.
   * @throws NotFoundException si la cuenta no existe.
   */
  @Transactional(readOnly = true)
  public List<FixedExpenseResponse> execute(UUID accountId, FixedExpenseFilterRequest filter) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    List<FixedExpense> expenses = fixedExpenseRepository.findAllByAccountId(accountId);

    if (filter != null) {
      if (filter.getStatus() != null) {
        expenses = expenses.stream()
            .filter(e -> e.getStatus() == filter.getStatus())
            .collect(Collectors.toList());
      }
      if (filter.getCategoryId() != null) {
        expenses = expenses.stream()
            .filter(e -> e.getCategoryId() != null && e.getCategoryId().equals(filter.getCategoryId()))
            .collect(Collectors.toList());
      }
      if (StringUtils.hasText(filter.getSearchTerm())) {
        String term = filter.getSearchTerm().toLowerCase();
        expenses = expenses.stream()
            .filter(e -> e.getName().toLowerCase().contains(term))
            .collect(Collectors.toList());
      }
    }

    // Carga en lote de categorías para evitar N+1
    Set<UUID> categoryIds = expenses.stream()
        .map(FixedExpense::getCategoryId)
        .collect(Collectors.toSet());
    Map<UUID, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));

    return expenses.stream()
        .map(e -> {
          Category cat = categoryMap.get(e.getCategoryId());
          CategoryResponse catResp = cat != null ? CategoryResponse.builder()
              .id(cat.getId())
              .name(cat.getName())
              .type(cat.getType())
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
        })
        .collect(Collectors.toList());
  }
}
