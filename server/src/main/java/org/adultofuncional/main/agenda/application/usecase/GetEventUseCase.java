package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener un evento por su identificador.
 *
 * <p>
 * Recupera el evento del repositorio verificando que pertenezca a la cuenta
 * indicada. Si el evento tiene una categoría asociada, carga sus datos desde
 * el módulo de finanzas y los anida como un {@link CategoryResponse}.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see EventRepository
 * @see CategoryRepository
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class GetEventUseCase {

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la consulta de un evento por su ID y el ID de su cuenta.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param eventId   Identificador único del evento.
   * @return {@link EventResponse} con los datos del evento y, opcionalmente,
   *         la categoría asociada.
   * @throws NotFoundException si el evento no existe o no pertenece a la
   *                           cuenta.
   */
  @Transactional(readOnly = true)
  public EventResponse execute(UUID accountId, UUID eventId) {
    Event event = eventRepository.findByIdAndAccountId(eventId, accountId)
        .orElseThrow(() -> new NotFoundException("Evento no encontrado con id: " + eventId));

    Category category = event.getCategoryId() != null
        ? categoryRepository.findById(event.getCategoryId()).orElse(null)
        : null;

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
