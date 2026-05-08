package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Eliminar un gasto fijo existente.
 *
 * <p>
 * Verifica que el gasto fijo exista en el sistema y, si es así, lo elimina
 * a través del puerto {@link FixedExpenseRepository}. La verificación y la
 * eliminación se ejecutan dentro de la misma transacción.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see FixedExpenseRepository
 * @see NotFoundException
 */
@Service
@RequiredArgsConstructor
public class DeleteFixedExpenseUseCase {

  /**
   * Puerto de dominio para la persistencia de gastos fijos.
   */
  private final FixedExpenseRepository fixedExpenseRepository;

  /**
   * Ejecuta la eliminación de un gasto fijo por su identificador.
   *
   * @param accountId Identificador de la cuenta propietaria (contexto de
   *                  trazabilidad; la eliminación se realiza por
   *                  {@code expenseId}).
   * @param expenseId Identificador único del gasto fijo a eliminar.
   * @throws NotFoundException si no existe un gasto fijo con el ID
   *                           proporcionado.
   */
  @Transactional
  public void execute(UUID accountId, UUID expenseId) {
    if (!fixedExpenseRepository.findById(expenseId).isPresent()) {
      throw new NotFoundException("Gasto fijo no encontrado con id: " + expenseId);
    }
    fixedExpenseRepository.deleteById(expenseId);
  }
}
