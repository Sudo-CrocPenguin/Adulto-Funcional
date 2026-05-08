package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso: Actualizar los datos de un gasto fijo existente.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>El gasto fijo debe existir y pertenecer a la cuenta indicada.</li>
 * <li>Solo se modifican los campos proporcionados en el DTO (actualización
 * parcial).</li>
 * <li>Si cambia la fecha de cierre, debe ser una fecha futura.</li>
 * <li>Si cambia la categoría, la nueva categoría debe existir.</li>
 * <li>El estado se gestiona explícitamente: si se envía {@code ACTIVE} se
 * activa el gasto; cualquier otro valor lo desactiva.</li>
 * </ul>
 *
 * <p>
 * Retorna un {@link FixedExpenseResponse} con la categoría anidada como
 * {@link CategoryResponse}, cargada tras la persistencia.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see FixedExpense
 * @see FixedExpenseRepository
 * @see CategoryRepository
 */
@Service
@RequiredArgsConstructor
public class UpdateFixedExpenseUseCase {

  /** Puerto de dominio para la persistencia de gastos fijos. */
  private final FixedExpenseRepository fixedExpenseRepository;

  /** Puerto de dominio para la validación de categorías. */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la actualización parcial de un gasto fijo.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param expenseId Identificador del gasto fijo a modificar.
   * @param request   DTO con los nuevos valores. Los campos nulos o vacíos
   *                  se ignoran.
   * @return {@link FixedExpenseResponse} con los datos actualizados,
   *         incluyendo la categoría asociada.
   * @throws NotFoundException si el gasto fijo no existe, no pertenece a la
   *                           cuenta, o la nueva categoría no existe.
   * @throws BusinessException si la nueva fecha de cierre no es futura.
   */
  @Transactional
  public FixedExpenseResponse execute(UUID accountId, UUID expenseId, UpdateFixedExpenseRequest request) {
    FixedExpense expense = fixedExpenseRepository.findById(expenseId)
        .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado con id: " + expenseId));

    if (!expense.getAccountId().equals(accountId)) {
      throw new NotFoundException("Gasto fijo no pertenece a la cuenta");
    }

    // Valores actuales como base para la actualización parcial
    String name = expense.getName();
    BigDecimal amount = expense.getAmount();
    UUID categoryId = expense.getCategoryId();
    var frequency = expense.getFrequency();
    var status = expense.getStatus();
    LocalDate startDate = expense.getStartDate();
    LocalDate nextDueDate = expense.getNextDueDate();
    int reminderDays = expense.getReminderDays();

    if (StringUtils.hasText(request.getName()))
      name = request.getName();
    if (request.getAmount() != null)
      amount = request.getAmount();
    if (request.getFrequency() != null)
      frequency = request.getFrequency();
    if (request.getNextDueDate() != null) {
      if (request.getNextDueDate().isBefore(LocalDate.now())) {
        throw new BusinessException("La fecha de cierre debe ser futura");
      }
      nextDueDate = request.getNextDueDate();
    }
    if (request.getCategoryId() != null) {
      categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
      categoryId = request.getCategoryId();
    }

    expense.update(name, amount, categoryId, frequency, startDate, nextDueDate, reminderDays);

    if (request.getStatus() != null) {
      if (request.getStatus() == Status.ACTIVE)
        expense.activate();
      else
        expense.deactivate();
    }

    FixedExpense saved = fixedExpenseRepository.save(expense);

    // La categoría ya fue validada, pero se carga de nuevo para construir la
    // respuesta
    Category category = categoryRepository.findById(saved.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));

    CategoryResponse categoryResponse = CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .build();

    return FixedExpenseResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .frequency(saved.getFrequency())
        .amount(saved.getAmount())
        .status(saved.getStatus())
        .nextDueDate(saved.getNextDueDate())
        .category(categoryResponse)
        .build();
  }
}
