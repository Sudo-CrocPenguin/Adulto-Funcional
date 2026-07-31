package org.adultofuncional.main.agenda.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
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
   * Busca todos los eventos asociados a una cuenta específica.
   *
   * @param accountId UUID de la cuenta propietaria.
   * @return lista de entidades {@code EventEntity} de esa cuenta;
   *         puede estar vacía si no hay eventos registrados.
   */
  List<EventEntity> findByAccount_AccountId(UUID accountId);

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
