package org.adultofuncional.main.agenda.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.agenda.application.dto.EventRequest;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Crear un nuevo evento en la agenda personal.
 *
 * ... (resto del javadoc igual) ...
 */
@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

  // ... (dependencias y TODOs igual) ...

  @Transactional
  public EventResponse execute(UUID accountId, EventRequest request) {
    // ... (validaciones igual) ...

    // 4. Valores por defecto y validación
    String priority = request.getPriority() != null && !request.getPriority().isBlank()
        ? request.getPriority()
        : "Media";
    validarPrioridad(priority);

    String status = request.getStatus() != null && !request.getStatus().isBlank()
        ? request.getStatus()
        : "Pendiente";
    validarEstado(status);

    // 5. Crear modelo de dominio
    Event event = Event.create(
        request.getTitle(),
        request.getDescription(),
        priority,
        request.getEventDate(),
        request.getFrequency(),
        request.getReminder(),
        request.getStartHour(),
        request.getEndHour(),
        status,
        categoryId,
        accountId);

    Event saved = eventRepository.save(event);

    // 6. Mapear respuesta
    return mapToResponse(saved, category);
  }

  // ... (validarPrioridad, validarEstado igual) ...

  private EventResponse mapToResponse(Event event, Category category) {
    CategoryResponse categoryResponse = null;
    if (category != null) {
      categoryResponse = CategoryResponse.builder()
          .id(category.getId())
          .name(category.getName())
          .type(category.getType())
          .build();
    }

    return EventResponse.builder()
        .id(event.getId())
        .title(event.getTitle())
        .priority(event.getPriority())
        .eventDate(event.getDate())
        .frequency(event.getFrequency())
        .reminder(event.getReminder())
        .startHour(event.getStartHour())
        .endHour(event.getEndHour())
        .description(event.getDescription())
        .status(event.getStatus())
        .category(categoryResponse)
        .build();
  }
}
