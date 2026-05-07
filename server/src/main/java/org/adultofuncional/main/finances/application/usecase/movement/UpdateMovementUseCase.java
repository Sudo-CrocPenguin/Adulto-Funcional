package org.adultofuncional.main.finances.application.usecase.movement;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.application.dto.movement.UpdateMovementRequest;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Caso de uso: Actualizar un movimiento existente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdateMovementUseCase {

    private final MovementRepository movementRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public MovementResponse execute(UUID accountId, UUID movementId, UpdateMovementRequest request) {
        MovementEntity entity = movementRepository.findByIdAndAccountId(movementId, accountId)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado: " + movementId));

        if (request.getMovementType() != null) {
            entity.setMovementType(request.getMovementType().name());
        }
        if (request.getAmount() != null) {
            entity.setMovementAmount(request.getAmount());
        }
        if (request.getMovementDate() != null) {
            entity.setMovementDate(request.getMovementDate());
        }
        if (request.getDescription() != null) {
            entity.setMovementDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + request.getCategoryId()));
            entity.setCategory(category);
        }

        MovementEntity updated = movementRepository.save(entity);
        return mapToResponse(updated);
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
