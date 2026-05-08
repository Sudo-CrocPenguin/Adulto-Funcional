package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Obtener un movimiento financiero por su identificador.
 *
 * <p>
 * Recupera el movimiento del repositorio y verifica que pertenezca a la
 * cuenta indicada antes de retornar sus datos. Si el movimiento no existe
 * o no pertenece a la cuenta, se lanza {@link NotFoundException} para
 * proteger la privacidad de los datos de otras cuentas.
 *
 * <p>
 * La categoría no se retorna en la respuesta actualmente (pendiente de
 * incluir en una versión futura).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see MovementRepository
 * @see Movement
 */
@Service
@RequiredArgsConstructor
public class GetMovementUseCase {

  /** Puerto de dominio para la consulta de movimientos. */
  private final MovementRepository movementRepository;

  /**
   * Ejecuta la consulta de un movimiento por su ID.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param movementId Identificador único del movimiento.
   * @return {@link MovementResponse} con los datos del movimiento.
   * @throws NotFoundException si el movimiento no existe o no pertenece a
   *                           la cuenta.
   */
  @Transactional(readOnly = true)
  public MovementResponse execute(UUID accountId, UUID movementId) {
    Movement movement = movementRepository.findById(movementId)
        .orElseThrow(() -> new NotFoundException("Movimiento no encontrado con id: " + movementId));

    if (!movement.getAccountId().equals(accountId)) {
      throw new NotFoundException("Movimiento no pertenece a la cuenta");
    }

    return MovementResponse.builder()
        .id(movement.getId())
        .movementType(movement.getType())
        .amount(movement.getAmount())
        .registerDate(movement.getCreatedAt())
        .description(movement.getDescription())
        .movementDate(movement.getDate())
        .category(null)
        .build();
  }
}
