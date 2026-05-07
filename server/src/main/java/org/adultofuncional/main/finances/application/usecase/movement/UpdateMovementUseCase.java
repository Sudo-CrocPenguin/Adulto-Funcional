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

@Service
@RequiredArgsConstructor
public class UpdateMovementUseCase {
    private final MovementRepository movementRepository;
    private final CategoryRepository categoryRepository;

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
                movement.getDate()
            );
        }
        if (request.getAmount() != null) {
            movement.update(
                movement.getType(),
                request.getAmount(),
                movement.getCategoryId(),
                movement.getDescription(),
                movement.getDate()
            );
        }
        if (StringUtils.hasText(request.getDescription())) {
            movement.update(
                movement.getType(),
                movement.getAmount(),
                movement.getCategoryId(),
                request.getDescription(),
                movement.getDate()
            );
        }
        if (request.getMovementDate() != null) {
            movement.update(
                movement.getType(),
                movement.getAmount(),
                movement.getCategoryId(),
                movement.getDescription(),
                request.getMovementDate()
            );
        }
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
            movement.update(
                movement.getType(),
                movement.getAmount(),
                request.getCategoryId(),
                movement.getDescription(),
                movement.getDate()
            );
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
