package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.movement.UpdateMovementRequest;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Caso de uso: Actualizar los datos de un movimiento financiero existente.
 *
 * <p>
 * Reglas de negocio:
 * <ul>
 * <li>El movimiento debe existir y pertenecer a la cuenta indicada.</li>
 * <li>Solo se modifican los campos proporcionados en el DTO (actualización
 * parcial).</li>
 * <li>Si cambia la categoría, la nueva categoría debe existir.</li>
 * </ul>
 *
 * <p>
 * La actualización se realiza invocando {@link Movement#update} por cada campo
 * modificado. Esto permite aplicar cambios de forma selectiva aunque implica
 * múltiples reasignaciones internas sobre la misma entidad.
 *
 * <p>
 * La categoría no se retorna en la respuesta actualmente (pendiente de
 * incluir en una versión futura).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see Movement
 * @see MovementRepository
 * @see CategoryRepository
 */
@Service
@RequiredArgsConstructor
public class UpdateMovementUseCase {

  /** Puerto de dominio para la persistencia de movimientos. */
  private final MovementRepository movementRepository;

  /** Puerto de dominio para la validación de categorías. */
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la actualización parcial de un movimiento.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param movementId Identificador del movimiento a modificar.
   * @param request    DTO con los nuevos valores. Los campos nulos o vacíos
   *                   se ignoran.
   * @return {@link MovementResponse} con los datos actualizados. La categoría
   *         se retorna como {@code null} en esta versión.
   * @throws NotFoundException si el movimiento no existe, no pertenece a la
   *                           cuenta, o la nueva categoría no existe.
   */
  @Transactional
  public MovementResponse execute(UUID accountId, UUID movementId, UpdateMovementRequest request) {
    Movement movement = movementRepository.findById(movementId)
        .orElseThrow(() -> new NotFoundException("Movimiento no encontrado con id: " + movementId));

    if (!movement.getAccountId().equals(accountId)) {
      throw new NotFoundException("Movimiento no pertenece a la cuenta");
    }

    if (request.getMovementType() != null) {
      movement.update(
          request.getMovementType(),
          movement.getAmount(),
          movement.getCategoryId(),
          movement.getDescription(),
          movement.getDate());
    }
    if (request.getAmount() != null) {
      movement.update(
          movement.getType(),
          request.getAmount(),
          movement.getCategoryId(),
          movement.getDescription(),
          movement.getDate());
    }
    if (StringUtils.hasText(request.getDescription())) {
      movement.update(
          movement.getType(),
          movement.getAmount(),
          movement.getCategoryId(),
          request.getDescription(),
          movement.getDate());
    }
    if (request.getMovementDate() != null) {
      movement.update(
          movement.getType(),
          movement.getAmount(),
          movement.getCategoryId(),
          movement.getDescription(),
          request.getMovementDate());
    }
    if (request.getCategoryId() != null) {
      categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new NotFoundException(
              "Categoría no encontrada con id: " + request.getCategoryId()));
      movement.update(
          movement.getType(),
          movement.getAmount(),
          request.getCategoryId(),
          movement.getDescription(),
          movement.getDate());
    }

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
