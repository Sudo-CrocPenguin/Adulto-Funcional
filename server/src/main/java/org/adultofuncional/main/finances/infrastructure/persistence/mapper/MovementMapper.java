package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.stereotype.Component;

/**
 * Componente que convierte entre las diferentes representaciones de un movimiento.
 *
 * <p>
 * Traduce entre:
 * <ul>
 * <li>{@link MovementEntity} (JPA) ↔ {@link Movement} (dominio)</li>
 * </ul>
 *
 * <p>
 * El {@code accountId} se obtiene directamente del modelo de dominio
 * {@link Movement} para construir la referencia JPA.
 *
 * <p>
 * El método {@code toResponse()} se implementará cuando los DTOs
 * de respuesta estén definidos por el equipo.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see Movement
 * @see MovementEntity
 */

@Component
public class MovementMapper {
    
    /**
   * Convierte una {@link MovementEntity} al modelo de dominio {@link Movement}.
   *
   * <p>
   * Usa el método de fábrica {@code Movement.reconstitute()} para respetar
   * el constructor privado del modelo de dominio.
   *
   * <p>
   * El campo {@code movementDate} ({@code LocalDate}) se convierte a
   * {@code LocalDateTime} usando {@code atStartOfDay()} para cumplir
   * con el tipo esperado por el dominio.
   *
   * <p>
   * Si la categoría es {@code null} (opcional), el {@code categoryId}
   * se establece como {@code null} en el dominio.
   *
   * @param entity entidad JPA; si es {@code null} retorna {@code null}
   * @return modelo de dominio reconstituido o {@code null}
   */

    public Movement toDomain(MovementEntity entity) {
        if (entity == null) return null;

        UUID categoryId = entity.getCategory() != null
            ? entity.getCategory().getCategoryId()
            : null;

        UUID accountId = entity.getAccount() != null
            ? entity.getAccount().getAccountId()
            : null;

        return Movement.reconstitute(
            entity.getMovementId(),
            MovementType.valueOf(entity.getMovementType()),
            entity.getMovementAmount(),
            categoryId,
            accountId,
            entity.getMovementDescription(),
            entity.getMovementDate(),
            entity.getMovementRegisterDate()
        );

    }


      /**
   * Convierte el modelo de dominio {@link Movement} a {@link MovementEntity}.
   *
   * <p>
   * El {@code accountId} se obtiene directamente de {@link Movement#getAccountId()}.
   * Se construyen referencias JPA con solo el ID para {@code account} y
   * {@code category}, suficiente para que Hibernate resuelva las FK al persistir.
   *
   * <p>
   * El campo {@code movementRegisterDate} no se asigna aquí porque la entidad
   * lo establece automáticamente mediante {@code @PrePersist}.
   *
   * @param movement  modelo de dominio; si es {@code null} retorna {@code null}
   * @return entidad JPA lista para persistir
   */

    public MovementEntity toEntity(Movement movement) {
        
        if (movement == null) return null;

        MovementEntity entity = new MovementEntity();
        entity.setMovementId(movement.getId());
        entity.setMovementType(movement.getType().name());
        entity.setMovementAmount(movement.getAmount());
        entity.setMovementDescription(movement.getDescription());
        entity.setMovementDate(movement.getDate());

        if (movement.getCategoryId() != null) {
            CategoryEntity categoryRef = new CategoryEntity();
            categoryRef.setCategoryId(movement.getCategoryId());
            entity.setCategory(categoryRef);
        }

        AccountEntity accountRef = new AccountEntity();
        accountRef.setAccountId(movement.getAccountId());
        entity.setAccount(accountRef);

        return entity;


    }
}
