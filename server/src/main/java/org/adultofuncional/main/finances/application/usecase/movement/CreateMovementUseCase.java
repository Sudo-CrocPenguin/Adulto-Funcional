package org.adultofuncional.main.finances.application.usecase.movement;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Crear un nuevo movimiento financiero.
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Servicio que encapsula la lógica para registrar una transacción financiera.
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Verifica la existencia de la cuenta y categoría, crea la entidad y la persiste.
 * <p>
 * <strong>¿Cómo funciona?</strong><br>
 * Recibe el ID de cuenta y el request DTO, valida, mapea y guarda usando el repositorio.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class CreateMovementUseCase {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    // TODO: Agregar validación de saldo disponible para egresos
    // TODO: Agregar logging de auditoría

    @Transactional
    public MovementResponse execute(UUID accountId, CreateMovementRequest request) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + accountId));

        CategoryEntity category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + request.getCategoryId()));
        }

        MovementEntity entity = new MovementEntity();
        entity.setAccount(account);
        entity.setMovementType(request.getMovementType().name());
        entity.setMovementAmount(request.getAmount());
        entity.setMovementDate(request.getMovementDate());
        entity.setMovementDescription(request.getDescription());
        entity.setCategory(category);

        MovementEntity saved = movementRepository.save(entity);
        return mapToResponse(saved);
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
