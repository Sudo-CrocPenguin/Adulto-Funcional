package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.application.dto.EventUpdateRequest;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Caso de uso: Actualizar un evento existente.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que modifica parcialmente los datos de un evento.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdateEventUseCase {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public EventResponse execute(UUID accountId, UUID eventId, EventUpdateRequest request) {
        var event = eventRepository.findByIdAndAccountId(eventId, accountId)
                .orElseThrow(() -> new NotFoundException("Evento no encontrado con id: " + eventId));

        if (StringUtils.hasText(request.getTitle())) {
            event.updateTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getPriority())) {
            event.updatePriority(request.getPriority());
        }
        if (request.getEventDate() != null) {
            event.updateEventDate(request.getEventDate());
        }
        if (request.getFrequency() != null) {
            event.updateFrequency(request.getFrequency());
        }
        if (request.getReminder() != null) {
            event.updateReminder(request.getReminder());
        }
        if (request.getStartHour() != null && request.getEndHour() != null) {
            if (request.getStartHour().isAfter(request.getEndHour())) {
                throw new BusinessException("La hora de inicio no puede ser posterior a la hora de fin");
            }
            event.updateStartHour(request.getStartHour());
            event.updateEndHour(request.getEndHour());
        } else if (request.getStartHour() != null) {
            event.updateStartHour(request.getStartHour());
        } else if (request.getEndHour() != null) {
            event.updateEndHour(request.getEndHour());
        }
        if (StringUtils.hasText(request.getDescription())) {
            event.updateDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getStatus())) {
            event.updateStatus(request.getStatus());
        }
        if (request.getCategoryId() != null) {
            var category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
            event.updateCategoryId(category.getCategoryId());
        }

        var updated = eventRepository.save(event);

        // Mapear respuesta
        var category = updated.getCategoryId() != null
                ? categoryRepository.findById(updated.getCategoryId()).orElse(null)
                : null;
        var catDto = category != null ? EventCategoryDTO.builder()
                .id(category.getCategoryId())
                .name(category.getCategoryName())
                .type(category.getCategoryType())
                .createdAt(category.getCategoryCreatedAt())
                .build() : null;

        return EventResponse.builder()
                .id(updated.getId())
                .title(updated.getTitle())
                .priority(updated.getPriority())
                .eventDate(updated.getEventDate())
                .frequency(updated.getFrequency())
                .reminder(updated.getReminder())
                .startHour(updated.getStartHour())
                .endHour(updated.getEndHour())
                .description(updated.getDescription())
                .status(updated.getStatus())
                .category(catDto)
                .build();
    }
}
