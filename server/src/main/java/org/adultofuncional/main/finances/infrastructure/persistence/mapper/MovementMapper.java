package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.stereotype.Component;


@Component
public class MovementMapper {
    
    public Movement toDomain(MovementEntity entity) {
        if (entity == null) return null;

        UUID categoryId = entity.getCategory() != null ? entity.getCategory().getCategoryId() : null;

        return Movement.reconstitute(
            entity.getMovementId(),
            MovementType.valueOf(entity.getMovementType()),
            entity.getMovementAmount(),
            categoryId,
            entity.getMovementDescription(),
            entity.getMovementDate().atStartOfDay(),
            entity.getMovementRegisterDate()
        );
    }

    public MovementEntity toEntity(Movement movement, UUID accountId) {
        if (movement == null) return null;

        MovementEntity entity = new MovementEntity();
        entity.setMovementId(movement.getId());
        entity.setMovementType(movement.getType().name());
        entity.setMovementAmount(movement.getAmount());
        entity.setMovementDescription(movement.getDescription());
        entity.setMovementDate(movement.getDate().toLocalDate());

        if (movement.getCategoryId() != null) {
            CategoryEntity categoryRef = new CategoryEntity();

            categoryRef.setCategoryId(movement.getCategoryId());
            entity.setCategory(categoryRef);
        }

        AccountEntity accountRef = new AccountEntity();
        accountRef.setAccountId(accountId);
        entity.setAccount(accountRef);

        return entity;

    }
}
