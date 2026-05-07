package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link MovementEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla {@code movements} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador correspondiente en la capa de infraestructura.
 * </p>
 *
 * @author Daniel Salazar
 * @see MovementEntity
 * @since 1.0
 */
public interface SpringMovementJpaRepository extends JpaRepository<MovementEntity, UUID> {

    /**
     * Busca todos los movimientos financieros asociados a una cuenta específica.
     *
     * @param accountId el identificador de la cuenta (UUID)
     * @return lista de entidades {@code MovementEntity} pertenecientes a la cuenta,
     *         puede estar vacía si no hay movimientos registrados
     */
    List<MovementEntity> findByAccount_AccountId(UUID accountId);

    /**
     * Busca un movimiento por su identificador único.
     * <p>
     * Método explícito similar a {@link #findById(Object)} pero con nombre
     * más semántico.
     * </p>
     *
     * @param movementId el UUID del movimiento
     * @return la entidad si existe, o {@code null} si no se encuentra
     */
    MovementEntity findByMovementId(UUID movementId);
}