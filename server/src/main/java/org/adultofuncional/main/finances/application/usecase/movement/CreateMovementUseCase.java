package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Registrar un nuevo movimiento financiero (ingreso o egreso)
 * en una cuenta.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>La cuenta debe existir.</li>
 * <li>Si se especifica una categoría, esta debe existir.</li>
 * </ul>
 *
 * <p>
 * La creación del modelo {@link Movement} se delega al método de fábrica
 * del dominio. La categoría no se retorna en la respuesta actualmente
 * (pendiente de incluir en una versión futura).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see Movement
 * @see MovementRepository
 * @see AccountRepository
 * @see CategoryRepository
 */
@Service
@RequiredArgsConstructor
public class CreateMovementUseCase {

  /** Puerto de dominio para la persistencia de movimientos. */
  private final MovementRepository movementRepository;

  /** Puerto de dominio para la validación de la cuenta (módulo account). */
  private final AccountRepository accountRepository;

  /** Puerto de dominio para la validación de la categoría. */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la creación de un nuevo movimiento.
   *
   * @param accountId Identificador de la cuenta en la que se registra el
   *                  movimiento.
   * @param request   DTO con los datos validados del movimiento (tipo,
   *                  monto, fecha, descripción y categoría opcional).
   * @return {@link MovementResponse} con los datos del movimiento creado.
   *         La categoría se retorna como {@code null} en esta versión.
   * @throws NotFoundException si la cuenta o la categoría (si se
   *                           proporcionó) no existen.
   */
  @Transactional
  public MovementResponse execute(UUID accountId, CreateMovementRequest request) {
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    UUID finalCategoryId = null;
    if (request.getCategoryId() != null) {
      finalCategoryId = request.getCategoryId();
      final UUID categoryIdToCheck = finalCategoryId;
      categoryRepository.findById(categoryIdToCheck)
          .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryIdToCheck));
    }

    Movement movement = Movement.create(
        request.getMovementType(),
        request.getAmount(),
        finalCategoryId,
        accountId,
        request.getDescription(),
        request.getMovementDate());

    Movement saved = movementRepository.save(movement);
    return MovementResponse.builder()
        .id(saved.getId())
        .movementType(saved.getType())
        .amount(saved.getAmount())
        .registerDate(saved.getCreatedAt())
        .description(saved.getDescription())
        .movementDate(saved.getDate())
        .category(null)
        .build();
  }
}
