package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Crear un nuevo gasto fijo recurrente asociado a una cuenta.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>La cuenta debe existir en el módulo de cuentas.</li>
 * <li>La categoría asociada debe existir.</li>
 * <li>La fecha de cierre debe ser posterior a la fecha actual.</li>
 * <li>La fecha de inicio se establece como la fecha actual y los días de
 * recordatorio se inicializan en 0 automáticamente.</li>
 * </ul>
 *
 * <p>
 * Retorna un {@link FixedExpenseResponse} con la categoría anidada como
 * {@link CategoryResponse}, construida a partir de la categoría recuperada
 * durante la validación.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see FixedExpense
 * @see FixedExpenseRepository
 * @see AccountRepository
 * @see CategoryRepository
 */
@Service
@RequiredArgsConstructor
public class CreateFixedExpenseUseCase {

  /** Puerto de dominio para la persistencia de gastos fijos. */
  private final FixedExpenseRepository fixedExpenseRepository;

  /** Puerto de dominio para la consulta de cuentas (módulo account). */
  private final AccountRepository accountRepository;

  /** Puerto de dominio para la consulta de categorías. */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la creación de un nuevo gasto fijo.
   *
   * @param accountId Identificador de la cuenta a la que se asocia el gasto.
   * @param request   DTO con los datos validados del gasto fijo.
   * @return {@link FixedExpenseResponse} con los datos del gasto creado,
   *         incluyendo la categoría asociada.
   * @throws NotFoundException si la cuenta o la categoría no existen.
   * @throws BusinessException si la fecha de cierre no es futura.
   */
  @Transactional
  public FixedExpenseResponse execute(UUID accountId, CreateFixedExpenseRequest request) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (request.getNextDueDate().isBefore(LocalDate.now())) {
      throw new BusinessException("La fecha de cierre debe ser posterior a la fecha actual");
    }

    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));

    FixedExpense expense = FixedExpense.create(
        request.getName(),
        request.getAmount(),
        request.getCategoryId(),
        accountId,
        request.getFrequency(),
        LocalDate.now(),
        request.getNextDueDate(),
        0);

    FixedExpense saved = fixedExpenseRepository.save(expense);

    CategoryResponse categoryResponse = CategoryResponse.builder()
        .id(category.getId())
        .name(saved.getName())
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
