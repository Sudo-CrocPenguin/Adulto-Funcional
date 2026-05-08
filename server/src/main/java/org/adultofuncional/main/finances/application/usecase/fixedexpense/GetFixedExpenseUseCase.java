package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Obtener un gasto fijo por su identificador.
 *
 * <p>
 * Recupera el gasto fijo del repositorio junto con su categoría asociada,
 * y retorna un {@link FixedExpenseResponse} que incluye la información
 * anidada de la categoría. Si la categoría no existe (por ejemplo, fue
 * eliminada después de asociarse), se lanza {@link NotFoundException}.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see FixedExpenseRepository
 * @see CategoryRepository
 * @see FixedExpenseResponse
 */
@Service
@RequiredArgsConstructor
public class GetFixedExpenseUseCase {

  /** Puerto de dominio para la consulta de gastos fijos. */
  private final FixedExpenseRepository fixedExpenseRepository;

  /** Puerto de dominio para la consulta de categorías. */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la consulta de un gasto fijo por su ID.
   *
   * @param accountId Identificador de la cuenta propietaria (contexto de
   *                  trazabilidad; la consulta se realiza por {@code expenseId}).
   * @param expenseId Identificador único del gasto fijo.
   * @return {@link FixedExpenseResponse} con los datos del gasto fijo y su
   *         categoría asociada como {@link CategoryResponse}.
   * @throws NotFoundException si el gasto fijo no existe o si la categoría
   *                           asociada no es encontrada.
   */
  @Transactional(readOnly = true)
  public FixedExpenseResponse execute(UUID accountId, UUID expenseId) {
    FixedExpense expense = fixedExpenseRepository.findById(expenseId)
        .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado con id: " + expenseId));

    Category category = categoryRepository.findById(expense.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Categoría asociada no encontrada"));

    CategoryResponse categoryResponse = CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .build();

    return FixedExpenseResponse.builder()
        .id(expense.getId())
        .name(expense.getName())
        .frequency(expense.getFrequency())
        .amount(expense.getAmount())
        .status(expense.getStatus())
        .nextDueDate(expense.getNextDueDate())
        .category(categoryResponse)
        .build();
  }
}
