package org.adultofuncional.main.agenda.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
   * Verifica si existe un evento con el ID dado y que pertenezca a
   * la cuenta indicada.
   *
   * @param eventId   UUID del evento.
   * @param accountId UUID de la cuenta propietaria.
   * @return {@code true} si el evento existe y pertenece a la cuenta.
   */
  boolean existsByEventIdAndAccount_AccountId(UUID eventId, UUID accountId);
}
