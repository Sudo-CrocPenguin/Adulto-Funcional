package org.adultofuncional.main.agenda.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA para la entidad {@link EventEntity}.
 *
 * <p>
 * Proporciona métodos de acceso a la tabla {@code events} sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador
 * {@link org.adultofuncional.main.agenda.infrastructure.repository.EventRepositoryImpl}
 * para traducir las operaciones del puerto
 * {@link org.adultofuncional.main.agenda.domain.repository.EventRepository}
 * a consultas JPA.
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see EventEntity
 */
public interface SpringEventJpaRepository extends JpaRepository<EventEntity, UUID> {

  /**
   * Consulta una página de eventos con ownership y filtros en SQL.
   *
   * @param accountId UUID de la cuenta propietaria.
   * @return página acotada y total de coincidencias
   */
  @EntityGraph(attributePaths = {"account", "category"})
  @Query("""
      SELECT event
      FROM EventEntity event
      WHERE event.account.accountId = :accountId
        AND (:status IS NULL OR event.eventStatus = :status)
        AND (:priority IS NULL OR event.eventPriority = :priority)
        AND (:categoryId IS NULL OR event.category.categoryId = :categoryId)
        AND (:startDate IS NULL OR event.eventDate >= :startDate)
        AND (:endDate IS NULL OR event.eventDate <= :endDate)
      """)
  Page<EventEntity> findPageByAccountId(
      @Param("accountId") UUID accountId,
      @Param("status") String status,
      @Param("priority") String priority,
      @Param("categoryId") UUID categoryId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      Pageable pageable);

  /**
   * Busca un evento por su ID y el ID de la cuenta propietaria.
   *
   * <p>
   * Garantiza que el evento pertenece a la cuenta indicada, evitando
   * fugas de información entre cuentas.
   *
   * @param eventId   UUID del evento.
   * @param accountId UUID de la cuenta propietaria.
   * @return {@link Optional} con la entidad si existe y pertenece a
   *         la cuenta; {@code Optional.empty()} en caso contrario.
   */
  Optional<EventEntity> findByEventIdAndAccount_AccountId(UUID eventId, UUID accountId);

  /**
   * Elimina de forma atómica un evento limitado por cuenta propietaria.
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      DELETE FROM events
      WHERE event_id = :eventId
        AND event_fk_account_id = :accountId
      """, nativeQuery = true)
  int deleteByEventIdAndAccountId(
      @Param("eventId") UUID eventId,
      @Param("accountId") UUID accountId);
}
