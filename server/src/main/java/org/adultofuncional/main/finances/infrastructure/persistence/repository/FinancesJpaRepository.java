package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link MovementEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla de movimientos sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador {@code JpaFinancesRepositoryAdapter}.
 * </p>
 *
 * @author Daniel Salazar
 * @see MovementEntity
 * @since 1.0
 */
public interface FinancesJpaRepository extends JpaRepository<MovementEntity, UUID> {

    /**
     * Busca movimientos por el ID de la cuenta propietaria.
     * @param accountId el ID de la cuenta (UUID)
     * @return lista de movimientos de esa cuenta
     */
    List<MovementEntity> findByAccount_AccountId(UUID accountId);

    /**
     * Busca un movimiento por su ID (similar a findById pero explícito).
     * @param movementId el ID del movimiento
     * @return la entidad si existe
     */
    MovementEntity findByMovementId(UUID movementId);
}