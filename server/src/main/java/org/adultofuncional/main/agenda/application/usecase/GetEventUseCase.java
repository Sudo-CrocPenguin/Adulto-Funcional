package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventCategoryDTO;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener un evento por su ID.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que recupera un evento específico de la cuenta.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetEventUseCase {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public EventResponse execute(UUID accountId, UUID eventId) {
        var event = eventRepository.findByIdAndAccountId(eventId, accountId)
                .orElseThrow(() -> new NotFoundException("Evento no encontrado con id: " + eventId));

        var category = event.getCategoryId() != null
                ? categoryRepository.findById(event.getCategoryId()).orElse(null)
                : null;

        EventCategoryDTO catDto = null;
        if (category != null) {
            catDto = EventCategoryDTO.builder()
                    .id(category.getCategoryId())
                    .name(category.getCategoryName())
                    .type(category.getCategoryType())
                    .createdAt(category.getCategoryCreatedAt())
                    .build();
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .priority(event.getPriority())
                .eventDate(event.getEventDate())
                .frequency(event.getFrequency())
                .reminder(event.getReminder())
                .startHour(event.getStartHour())
                .endHour(event.getEndHour())
                .description(event.getDescription())
                .status(event.getStatus())
                .category(catDto)
                .build();
    }
}
