package org.adultofuncional.main.agenda.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link EventEntity}.
 * <p>
 * Proporciona métodos de acceso a la tabla de eventos sin exponer
 * la implementación concreta al dominio. Este repositorio es utilizado
 * por el adaptador {@code JpaAgendaRepositoryAdapter}.
 * </p>
 *
 * @author Daniel Salazar
 * @see EventEntity
 * @since 1.0
 */
public interface AgendaJpaRepository extends JpaRepository<EventEntity, UUID> {

    /**
     * Busca eventos por el ID de la cuenta propietaria.
     * @param accountId el ID de la cuenta (UUID)
     * @return lista de eventos de esa cuenta
     */
    List<EventEntity> findByAccount_AccountId(UUID accountId);

    /**
     * Busca un evento por su ID (similar a findById pero explícito).
     * @param eventId el ID del evento
     * @return la entidad si existe
     */
    EventEntity findByEventId(UUID eventId);
}