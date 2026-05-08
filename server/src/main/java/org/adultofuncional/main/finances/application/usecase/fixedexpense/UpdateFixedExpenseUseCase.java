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
 * Caso de uso responsable de la actualización parcial o total de la información
 * de un gasto fijo existente en el sistema.
 *
 * <p>
 * Esta clase implementa la lógica de actualización dentro de la capa de
 * aplicación, gestionando la validación de reglas de negocio (como fechas
 * futuras), la verificación de integridad de categorías y la sincronización
 * del estado de la entidad.
 * </p>
 *
 * <p>
 * <b>¿Qué es?</b><br>
 * Un servicio de aplicación de responsabilidad única que encapsula la lógica
 * del caso de uso "actualizar un gasto fijo". Está gestionado por el contenedor
 * de Spring mediante {@code @Service} e inyecta sus dependencias de forma
 * automática mediante {@code @RequiredArgsConstructor}.
 * </p>
 *
 * <p>
 * <b>¿Para qué sirve?</b><br>
 * Recibe una solicitud de modificación, localiza la entidad actual, valida que
 * los nuevos datos cumplan con las restricciones del dominio (especialmente en
 * fechas y categorías existentes) y persiste los cambios de forma segura,
 * retornando una vista actualizada del recurso.
 * </p>
 *
 * <p>
 * <b>¿Cómo funciona?</b><br>
 * El método {@code execute} implementa el siguiente flujo operativo:
 * </p>
 * <ol>
 * <li>Recupera la entidad {@link FixedExpense} por su identificador único;
 * si no existe, lanza {@link NotFoundException}.</li>
 * <li>Extrae los valores actuales y los sobrescribe únicamente si los campos
 * en el {@link UpdateFixedExpenseRequest} están presentes (no nulos o con
 * texto).</li>
 * <li>Valida que, si se intenta cambiar la fecha de cierre, esta sea posterior
 * a la fecha actual, lanzando {@link BusinessException} en caso contrario.</li>
 * <li>Si se proporciona una nueva categoría, valida su existencia en el
 * {@link CategoryRepository}.</li>
 * <li>Invoca el método de actualización del dominio y gestiona el cambio de
 * estado (activar/desactivar) según el valor solicitado.</li>
 * <li>Persiste los cambios y transforma el resultado a
 * {@link FixedExpenseResponse}.</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 */
@Service
@RequiredArgsConstructor
public class UpdateFixedExpenseUseCase {

  /**
   * Repositorio de gastos fijos utilizado para la persistencia y recuperación
   * de la entidad a modificar.
   */
  private final FixedExpenseRepository fixedExpenseRepository;

  /**
   * Repositorio de categorías utilizado para validar que el identificador de
   * categoría proporcionado en la actualización sea válido y existente.
   */
  private final CategoryRepository categoryRepository;

  // TODO: Evaluar si el accountId recibido debe validarse contra el propietario
  // real del gasto fijo
  /**
   * Ejecuta la lógica de actualización del gasto fijo con los datos
   * proporcionados.
   *
   * <p>
   * La operación se realiza bajo un contexto transaccional, asegurando que
   * la validación de la categoría y la persistencia de la entidad se ejecuten
   * de forma atómica. Si alguna validación de negocio falla, se realiza un
   * rollback automático de la operación.
   * </p>
   *
   * @param accountId identificador UUID de la cuenta (usado para contexto de
   *                  seguridad).
   * @param expenseId identificador UUID del gasto fijo que se desea modificar.
   * @param request   objeto {@link UpdateFixedExpenseRequest} con los nuevos
   *                  valores.
   * @return {@link FixedExpenseResponse} con los datos actualizados del gasto.
   * @throws NotFoundException si el gasto fijo o la categoría no existen.
   * @throws BusinessException si la fecha de cierre proporcionada no es futura.
   */
  @Transactional
  public FixedExpenseResponse execute(UUID accountId, UUID expenseId, UpdateFixedExpenseRequest request) {
    FixedExpense expense = fixedExpenseRepository.findById(expenseId)
        .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado con id: " + expenseId));

    if (!expense.getAccountId().equals(accountId)) {
      throw new NotFoundException("Gasto fijo no pertenece a la cuenta");
    }

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

    Category category = categoryRepository.findById(saved.getCategoryId())
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada")); // No debería fallar

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
