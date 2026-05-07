package org.adultofuncional.main.finances.application.usecase.movement;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener un movimiento por su ID.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetMovementUseCase {

    private final MovementRepository movementRepository;

    @Transactional(readOnly = true)
    public MovementResponse execute(UUID accountId, UUID movementId) {
        MovementEntity entity = movementRepository.findByIdAndAccountId(movementId, accountId)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado: " + movementId));
        return mapToResponse(entity);
    }

    private MovementResponse mapToResponse(MovementEntity entity) {
        CategoryResponse categoryResponse = null;
        if (entity.getCategory() != null) {
            categoryResponse = CategoryResponse.builder()
                    .id(entity.getCategory().getCategoryId())
                    .name(entity.getCategory().getCategoryName())
                    .type(org.adultofuncional.main.finances.domain.enums.CategoryType.valueOf(entity.getCategory().getCategoryType()))
                    .createdAt(entity.getCategory().getCategoryCreatedAt())
                    .deleted(entity.getCategory().getCategoryDeletedAt() != null)
                    .build();
        }
        return MovementResponse.builder()
                .id(entity.getMovementId())
                .movementType(org.adultofuncional.main.finances.domain.enums.MovementType.valueOf(entity.getMovementType()))
                .amount(entity.getMovementAmount())
                .registerDate(entity.getMovementRegisterDate())
                .description(entity.getMovementDescription())
                .movementDate(entity.getMovementDate())
                .category(categoryResponse)
                .build();
    }
}
