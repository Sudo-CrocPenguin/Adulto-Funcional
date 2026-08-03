package org.adultofuncional.main.finances.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
   * Consulta una página de movimientos con ownership y filtros en SQL.
   *
   * @param accountId el identificador de la cuenta (UUID)
   * @return página acotada y total de coincidencias
   */
  @EntityGraph(attributePaths = {"account", "category"})
  @Query("""
      SELECT movement
      FROM MovementEntity movement
      WHERE movement.account.accountId = :accountId
        AND (:startDate IS NULL OR movement.movementDate >= :startDate)
        AND (:endDate IS NULL OR movement.movementDate <= :endDate)
        AND (:movementType IS NULL OR movement.movementType = :movementType)
        AND (:categoryId IS NULL OR movement.category.categoryId = :categoryId)
        AND (:searchTerm IS NULL OR LOWER(COALESCE(movement.movementDescription, ''))
             LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      """)
  Page<MovementEntity> findPageByAccountId(
      @Param("accountId") UUID accountId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("movementType") String movementType,
      @Param("categoryId") UUID categoryId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);
}
