package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMovementUseCase {
    private final MovementRepository movementRepository;

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
