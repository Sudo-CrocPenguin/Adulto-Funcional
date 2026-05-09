package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteMovementUseCase {

  private final MovementRepository movementRepository;

  @Transactional
  public void execute(UUID accountId, UUID movementId) {
    movementRepository.findById(movementId)
        .filter(movement -> movement.getAccountId().equals(accountId))
        .orElseThrow(() -> new NotFoundException("Movimiento no encontrado con id: " + movementId));

    movementRepository.deleteById(movementId);
  }
}
