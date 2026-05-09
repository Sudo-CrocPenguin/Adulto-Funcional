package org.adultofuncional.main.agenda.infrastructure.persistence.mapper;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper responsable de la conversión bidireccional entre la entidad JPA
 * {@link EventEntity} y el modelo de dominio {@link Event}.
 *
 * <h2>Estrategias de conversión</h2>
 * <ul>
 * <li>{@link #toDomain(EventEntity)} — utiliza {@link Event#reconstitute} para
 * reconstruir el agregado desde persistencia sin regenerar su identidad.</li>
 * <li>{@link #toEntity(Event)} — construye una {@link EventEntity} con
 * referencias parciales a {@link AccountEntity} y {@link CategoryEntity}
 * (solo ID), apoyándose en el contexto de persistencia de JPA para
 * resolver las relaciones {@code @ManyToOne} sin lanzar consultas
 * adicionales.</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see Event
 * @see EventEntity
 */

@Component
public class EventMapper {

    /**
     * Convierte una entidad JPA {@link EventEntity} en el modelo de dominio
     * {@link Event}.
     *
     * <p>
     * Delega en {@link Event#reconstitute} para garantizar que el dominio
     * controla la construcción del objeto sin regenerar su identidad.
     * La categoría puede ser {@code null} si el evento no tiene ninguna
     * asociada; en ese caso se pasa {@code null} al modelo de dominio.
     *
     * @param entity entidad JPA leída desde la base de datos. No debe ser
     *               {@code null}.
     * @return instancia de {@link Event} con el estado persistido.
     */

    public Event toDomain(EventEntity entity) {
        return Event.reconstitute(
                entity.getEventId(),
                entity.getEventTitle(),
                entity.getEventDescription(),
                entity.getEventPriority(),
                entity.getEventDate(),
                entity.getEventFrequency(),
                entity.getEventReminder(),
                entity.getEventStartHour(),
                entity.getEventEndHour(),
                entity.getEventStatus(),
                entity.getCategory() != null ? entity.getCategory().getCategoryId() : null,
                entity.getAccount().getAccountId()
        );
    }

    /**
     * Convierte el modelo de dominio {@link Event} en una entidad JPA
     * {@link EventEntity} lista para ser persistida.
     *
     * <p>
     * Las relaciones {@code @ManyToOne} con {@link AccountEntity} y
     * {@link CategoryEntity} se establecen creando instancias con únicamente
     * el ID asignado. Hibernate resuelve estas referencias a través del
     * contexto de persistencia sin ejecutar consultas {@code SELECT}
     * adicionales, equivalente a llamar a
     * {@code EntityManager.getReference()}.
     *
     * @param domain modelo de dominio a convertir. No debe ser {@code null}.
     * @return entidad JPA lista para {@code save} o {@code merge}.
     */

    public EventEntity toEntity(Event domain) {
        EventEntity entity = new EventEntity();

        entity.setEventId(domain.getId());
        entity.setEventTitle(domain.getTitle());
        entity.setEventDescription(domain.getDescription());
        entity.setEventPriority(domain.getPriority());
        entity.setEventDate(domain.getDate());
        entity.setEventFrequency(domain.getFrequency());
        entity.setEventReminder(domain.getReminder());
        entity.setEventStartHour(domain.getStartHour());
        entity.setEventEndHour(domain.getEndHour());
        entity.setEventStatus(domain.getStatus());

        AccountEntity account = new AccountEntity();
        account.setAccountId(domain.getAccountId());
        entity.setAccount(account);

        if (domain.getCategoryId() != null) {
            CategoryEntity category = new CategoryEntity();
            category.setCategoryId(domain.getCategoryId());
            entity.setCategory(category);
        }

        return entity;
    }
}