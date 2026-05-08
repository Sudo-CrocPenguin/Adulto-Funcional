package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.application.dto.EventUpdateRequest;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso: Actualizar un evento existente mediante PATCH.
 *
 * <p>
 * Aplica cambios parciales sobre un evento de la agenda. Solo los campos
 * proporcionados en el DTO son modificados; los demás conservan su valor
 * actual. Se valida que el evento pertenezca a la cuenta indicada y que,
 * si se actualizan las horas, la hora de inicio sea anterior a la de fin.
 *
 * <p>
 * <strong>Reglas de negocio aplicadas:</strong>
 * <ul>
 * <li>El evento debe existir y pertenecer a la cuenta.</li>
 * <li>Si se proporciona una nueva categoría, debe existir en el módulo de
 * finanzas.</li>
 * <li>Si se proporcionan ambas horas, se exige coherencia cronológica.</li>
 * <li>Los campos de texto vacíos o nulos se ignoran (validación con
 * {@link StringUtils#hasText}).</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see EventRepository
 * @see CategoryRepository
 * @see EventUpdateRequest
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class UpdateEventUseCase {

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta la actualización parcial del evento.
   *
   * @param accountId Identificador de la cuenta propietaria.
   * @param eventId   Identificador del evento a modificar.
   * @param request   DTO con los valores a actualizar (campos nulos o vacíos se
   *                  ignoran).
   * @return {@link EventResponse} con los datos actualizados, incluyendo la
   *         categoría asociada.
   * @throws NotFoundException si el evento no existe o la nueva categoría no
   *                           existe.
   * @throws BusinessException si la hora de inicio es posterior a la de fin.
   */
  @Transactional
  public EventResponse execute(UUID accountId, UUID eventId, EventUpdateRequest request) {
    Event event = eventRepository.findByIdAndAccountId(eventId, accountId)
        .orElseThrow(() -> new NotFoundException("Evento no encontrado con id: " + eventId));

    // Valores actuales que se usarán como base; se sobrescriben si hay cambios
    String title = event.getTitle();
    String description = event.getDescription();
    String priority = event.getPriority();
    LocalDate date = event.getDate();
    int frequency = event.getFrequency();
    LocalDateTime reminder = event.getReminder();
    LocalDateTime startHour = event.getStartHour();
    LocalDateTime endHour = event.getEndHour();
    String status = event.getStatus();
    UUID categoryId = event.getCategoryId();

    // Aplicar cambios solo si el campo fue proporcionado
    if (StringUtils.hasText(request.getTitle())) {
      title = request.getTitle();
    }
    if (StringUtils.hasText(request.getPriority())) {
      priority = request.getPriority();
    }
    if (request.getEventDate() != null) {
      date = request.getEventDate();
    }
    if (request.getFrequency() != null) {
      frequency = request.getFrequency();
    }
    if (request.getReminder() != null) {
      reminder = request.getReminder();
    }
    if (StringUtils.hasText(request.getDescription())) {
      description = request.getDescription();
    }
    if (StringUtils.hasText(request.getStatus())) {
      status = request.getStatus();
    }
    if (request.getCategoryId() != null) {
      categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
      categoryId = request.getCategoryId();
    }

    // Validar coherencia de horas si ambas se enviaron, o usar combinaciones
    // parciales
    if (request.getStartHour() != null) {
      startHour = request.getStartHour();
    }
    if (request.getEndHour() != null) {
      endHour = request.getEndHour();
    }
    // Validación final de horas (se delega al dominio, pero también la hacemos aquí
    // para garantizar el mensaje de negocio)
    if (startHour != null && endHour != null && startHour.isAfter(endHour)) {
      throw new BusinessException("La hora de inicio no puede ser posterior a la hora de fin");
    }

    // Actualizar todo el evento con los valores finales
    event.update(title, description, priority, date, frequency,
        reminder, startHour, endHour, status, categoryId);

    Event updated = eventRepository.save(event);

    // Construir categoría anidada si existe
    CategoryResponse categoryResponse = null;
    if (updated.getCategoryId() != null) {
      Category cat = categoryRepository.findById(updated.getCategoryId()).orElse(null);
      if (cat != null) {
        categoryResponse = CategoryResponse.builder()
            .id(cat.getId())
            .name(cat.getName())
            .type(cat.getType())
            .build();
      }
    }

    return EventResponse.builder()
        .id(updated.getId())
        .title(updated.getTitle())
        .priority(updated.getPriority())
        .eventDate(updated.getDate())
        .frequency(updated.getFrequency())
        .reminder(updated.getReminder())
        .startHour(updated.getStartHour())
        .endHour(updated.getEndHour())
        .description(updated.getDescription())
        .status(updated.getStatus())
        .category(categoryResponse)
        .build();
  }
}
