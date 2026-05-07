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
        // Verificar cuenta
        accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // Categoría opcional
        UUID categoryId = null;
        if (request.getCategoryId() != null) {
            categoryId = request.getCategoryId();
            categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));
        }

        Movement movement = Movement.create(
            request.getMovementType(),
            request.getAmount(),
            categoryId,
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
            .category(null) // por simplicidad, no cargamos la categoría aquí
            .build();
    }
}
