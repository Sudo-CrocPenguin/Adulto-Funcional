package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.agenda.application.dto.EventCategoryDTO;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar eventos de agenda con filtros opcionales.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un servicio de aplicación que encapsula la lógica de negocio para recuperar
 * los eventos de agenda de una cuenta, con soporte para filtros por fecha,
 * estado, prioridad y categoría.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para:
 * <ul>
 *   <li>Validar que la cuenta del usuario exista</li>
 *   <li>Recuperar eventos con filtros opcionales</li>
 *   <li>Ordenar eventos por fecha (más reciente primero o más antiguo primero)</li>
 *   <li>Devolver una lista de eventos con sus datos y categorías asociadas</li>
 * </ul>
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * <ol>
 *   <li>Recibe el ID de la cuenta del usuario autenticado</li>
 *   <li>Verifica que la cuenta exista en el repositorio</li>
 *   <li>Aplica filtros si se proporcionan (fecha, estado, prioridad, categoría)</li>
 *   <li>Recupera los eventos desde el repositorio</li>
 *   <li>Mapea cada entidad a un DTO de respuesta</li>
 *   <li>Retorna la lista de eventos</li>
 * </ol>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.domain.repository.EventRepository
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class ListEventsUseCase {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;

    // TODO: Agregar paginación para listas grandes
    // TODO: Agregar filtros por fecha (rango de fechas)
    // TODO: Agregar ordenamiento personalizable
    // TODO: Agregar soporte para expandir eventos recurrentes en el rango de fechas

    /**
     * Lista todos los eventos de una cuenta (sin filtros).
     *
     * @param accountId Identificador de la cuenta.
     * @return Lista de {@link EventResponse} con todos los eventos.
     * @throws NotFoundException Si no existe la cuenta.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> execute(UUID accountId) {
        return execute(accountId, null, null, null, null, null);
    }

    /**
     * Lista eventos de una cuenta con filtros opcionales.
     *
     * @param accountId   Identificador de la cuenta.
     * @param startDate   Fecha de inicio para filtrar (opcional).
     * @param endDate     Fecha de fin para filtrar (opcional).
     * @param status      Estado del evento (opcional).
     * @param priority    Prioridad del evento (opcional).
     * @param categoryId  ID de la categoría (opcional).
     * @return Lista filtrada de {@link EventResponse}.
     * @throws NotFoundException Si no existe la cuenta.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> execute(UUID accountId,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        String status,
                                        String priority,
                                        UUID categoryId) {
        // 1. Verificar que la cuenta existe
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        // 2. Recuperar eventos con filtros
        List<EventEntity> events;

        if (startDate != null && endDate != null) {
            events = eventRepository.findByAccountIdAndEventDateBetween(accountId, startDate, endDate);
        } else if (startDate != null) {
            events = eventRepository.findByAccountIdAndEventDateAfter(accountId, startDate);
        } else if (endDate != null) {
            events = eventRepository.findByAccountIdAndEventDateBefore(accountId, endDate);
        } else {
            events = eventRepository.findAllByAccountIdOrderByEventDateDesc(accountId);
        }

        // 3. Aplicar filtros adicionales (en memoria por simplicidad)
        // TODO: Mover estos filtros a consultas JPA para mejor rendimiento
        if (status != null && !status.isBlank()) {
            events = events.stream()
                    .filter(e -> e.getEventStatus().equals(status))
                    .collect(Collectors.toList());
        }

        if (priority != null && !priority.isBlank()) {
            events = events.stream()
                    .filter(e -> e.getEventPriority().equals(priority))
                    .collect(Collectors.toList());
        }

        if (categoryId != null) {
            events = events.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // 4. Mapear a DTOs
        return events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad {@link EventEntity} en un DTO {@link EventResponse}.
     */
    private EventResponse mapToResponse(EventEntity entity) {
        EventCategoryDTO categoryDTO = null;
        if (entity.getCategory() != null) {
            categoryDTO = EventCategoryDTO.builder()
                    .id(entity.getCategory().getCategoryId())
                    .name(entity.getCategory().getCategoryName())
                    .type(entity.getCategory().getCategoryType())
                    .createdAt(entity.getCategory().getCategoryCreatedAt())
                    .build();
        }

        return EventResponse.builder()
                .id(entity.getEventId())
                .title(entity.getEventTitle())
                .priority(entity.getEventPriority())
                .eventDate(entity.getEventDate())
                .frequency(entity.getEventFrequency())
                .reminder(entity.getEventReminder())
                .startHour(entity.getEventStartHour())
                .endHour(entity.getEventEndHour())
                .description(entity.getEventDescription())
                .status(entity.getEventStatus())
                .category(categoryDTO)
                .build();
    }
}