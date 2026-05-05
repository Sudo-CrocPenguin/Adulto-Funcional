/* Descomentar cuadndo se cree la entidad de finanzas
package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancesJpaRepository extends JpaRepository<FinancesEntity, UUID> { 

    /**
     * Busca movimientos por el ID de la cuenta propietaria.
     * @param accountId el ID de la cuenta (UUID)
     * @return lista de movimientos de esa cuenta
     */
// Descomentar:    List<MovementEntity> findByAccount_AccountId(UUID accountId);

    /**
     * Busca un movimiento por su ID (similar a findById pero explícito).
     * @param movementId el ID del movimiento
     * @return la entidad si existe
     */
// Descomentar:    MovementEntity findByMovementId(UUID movementId);

// Descomentar: }