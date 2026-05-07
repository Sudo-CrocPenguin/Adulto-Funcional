package org.adultofuncional.main.finances.application.usecase.movement;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar un movimiento financiero.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class DeleteMovementUseCase {

    private final MovementRepository movementRepository;

    @Transactional
    public void execute(UUID accountId, UUID movementId) {
        if (!movementRepository.existsByIdAndAccountId(movementId, accountId)) {
            throw new NotFoundException("Movimiento no encontrado: " + movementId);
        }
        movementRepository.deleteById(movementId);
    }
}
