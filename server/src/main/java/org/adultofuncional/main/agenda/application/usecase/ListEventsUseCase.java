package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventCategoryDTO;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar eventos con filtros opcionales.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Servicio que recupera todos los eventos de una cuenta.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Permite filtrar por estado, prioridad, categoría o rango de fechas.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ListEventsUseCase {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    // TODO: Paginación, filtros por rango de fechas, ordenamiento

    @Transactional(readOnly = true)
    public List<EventResponse> execute(UUID accountId, String status, String priority, UUID categoryId) {
        // Por simplicidad, se puede construir una consulta dinámica. Aquí se filtran en memoria.
        var events = eventRepository.findAllByAccountId(accountId);

        if (status != null && !status.isBlank()) {
            events = events.stream().filter(e -> e.getStatus().equals(status)).collect(Collectors.toList());
        }
        if (priority != null && !priority.isBlank()) {
            events = events.stream().filter(e -> e.getPriority().equals(priority)).collect(Collectors.toList());
        }
        if (categoryId != null) {
            events = events.stream().filter(e -> e.getCategoryId() != null && e.getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Mapear categorías
        var categoriesMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getCategoryId(), c -> c));

        return events.stream().map(event -> {
            var cat = categoriesMap.get(event.getCategoryId());
            EventCategoryDTO catDto = null;
            if (cat != null) {
                catDto = EventCategoryDTO.builder()
                        .id(cat.getCategoryId())
                        .name(cat.getCategoryName())
                        .type(cat.getCategoryType())
                        .createdAt(cat.getCategoryCreatedAt())
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
        }).collect(Collectors.toList());
    }
}
