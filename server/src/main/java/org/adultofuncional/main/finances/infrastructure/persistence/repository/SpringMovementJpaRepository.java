package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
   * Busca un movimiento por identificador y cuenta propietaria.
   */
  Optional<MovementEntity> findByMovementIdAndAccount_AccountId(
      UUID movementId,
      UUID accountId);

  /**
   * Elimina de forma atómica un movimiento limitado por cuenta.
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      DELETE FROM movements
      WHERE movement_id = :movementId
        AND movement_fk_account_id = :accountId
      """, nativeQuery = true)
  int deleteByMovementIdAndAccountId(
      @Param("movementId") UUID movementId,
      @Param("accountId") UUID accountId);

  /**
   * Busca todos los movimientos financieros asociados a una cuenta específica.
   *
   * @param accountId el identificador de la cuenta (UUID)
   * @return lista de entidades {@code MovementEntity} pertenecientes a la cuenta,
   *         puede estar vacía si no hay movimientos registrados
   */
  List<MovementEntity> findByAccount_AccountId(UUID accountId);
}
