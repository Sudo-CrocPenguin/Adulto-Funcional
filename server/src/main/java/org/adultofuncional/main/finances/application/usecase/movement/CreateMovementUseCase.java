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

@Service
@RequiredArgsConstructor
public class CreateMovementUseCase {
    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

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
            request.getMovementDate()
        );

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
