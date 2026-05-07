package org.adultofuncional.main.finances.application.usecase.movement;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar movimientos con filtros y paginación.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ListMovementsUseCase {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;

    // TODO: Implementar paginación y ordenamiento en repositorio

    @Transactional(readOnly = true)
    public List<MovementResponse> execute(UUID accountId, MovementFilterRequest filter) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + accountId));

        List<MovementEntity> movements = movementRepository.findAllByAccountIdOrderByMovementDateDesc(accountId);

        // Filtros en memoria (mover a repositorio cuando sea necesario)
        if (filter != null) {
            if (filter.getMovementType() != null) {
                movements = movements.stream()
                        .filter(m -> m.getMovementType().equals(filter.getMovementType().name()))
                        .collect(Collectors.toList());
            }
            if (filter.getCategoryId() != null) {
                movements = movements.stream()
                        .filter(m -> m.getCategory() != null && m.getCategory().getCategoryId().equals(filter.getCategoryId()))
                        .collect(Collectors.toList());
            }
            if (filter.getStartDate() != null) {
                movements = movements.stream()
                        .filter(m -> !m.getMovementDate().isBefore(filter.getStartDate()))
                        .collect(Collectors.toList());
            }
            if (filter.getEndDate() != null) {
                movements = movements.stream()
                        .filter(m -> !m.getMovementDate().isAfter(filter.getEndDate()))
                        .collect(Collectors.toList());
            }
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()) {
                String term = filter.getSearchTerm().toLowerCase();
                movements = movements.stream()
                        .filter(m -> m.getMovementDescription() != null && m.getMovementDescription().toLowerCase().contains(term))
                        .collect(Collectors.toList());
            }
        }

        return movements.stream().map(this::mapToResponse).collect(Collectors.toList());
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
