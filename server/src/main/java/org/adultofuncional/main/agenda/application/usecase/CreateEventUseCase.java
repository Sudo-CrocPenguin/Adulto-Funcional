package org.adultofuncional.main.agenda.application.usecase;

import java.time.ZoneId;
import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.agenda.application.dto.EventRequest;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.application.service.AgendaTimePolicy;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: Crear un nuevo evento en la agenda personal.
 *
 * <p>
 * Registra un evento asociado a una cuenta y a una categoría de agenda
 * accesible. Antes de persistir, aplica la política temporal en la zona IANA
 * elegida y delega las invariantes combinadas al dominio.
 *
 * <p>
 * <strong>Reglas de negocio aplicadas:</strong>
 * <ul>
 * <li>La cuenta debe existir en el módulo de cuentas.</li>
 * <li>{@code categoryId} es obligatorio; debe ser accesible para la cuenta y
 * de tipo {@code AGENDA}.</li>
 * <li>{@code startHour} debe ser anterior a {@code endHour} (no se permiten
 * horas iguales).</li>
 * <li>La prioridad, si no se envía o está en blanco, se asigna como
 * {@code "Media"}.</li>
 * <li>El estado, si no se envía o está en blanco, se asigna como
 * {@code "Pendiente"}.</li>
 * <li>Solo se aceptan los valores predefinidos para prioridad
 * ({@code Baja}, {@code Media}, {@code Alta}) y estado
 * ({@code Pendiente}, {@code Completado}, {@code Cancelado},
 * {@code Pospuesto}).</li>
 * <li>La frecuencia debe estar soportada, el recordatorio debe preceder el
 * inicio y las horas civiles deben corresponder a {@code eventDate}.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see Event
 * @see EventRepository
 * @see AccountRepository
 * @see CategoryRepository
 * @see EventRequest
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

  private final EventRepository eventRepository;
  private final AccountRepository accountRepository;
  private final CategoryRepository categoryRepository;
  private final AgendaTimePolicy timePolicy;

  /**
   * Ejecuta la creación de un nuevo evento.
   *
   * @param accountId Identificador de la cuenta propietaria. No puede ser
   *                  {@code null}.
   * @param request   DTO con los datos del evento validados.
   * @return {@link EventResponse} con los datos del evento creado,
   *         incluyendo la categoría anidada si fue asignada.
   * @throws NotFoundException si la cuenta no existe o si la categoría
   *                           proporcionada no existe.
   * @throws IllegalArgumentException si el horario, la frecuencia, la
   *                                  prioridad o el estado incumplen las
   *                                  invariantes del evento.
   */
  @Transactional
  public EventResponse execute(UUID accountId, EventRequest request) {
    // 1. Verificar cuenta
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    // 2. Buscar categoría obligatoria
    Category category = categoryRepository.findAccessibleByIdAndType(
            accountId,
            request.getCategoryId(),
            CategoryType.AGENDA)
        .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: "
            + request.getCategoryId()));
    UUID categoryId = category.getId();

    // 3. Valores por defecto. El dominio valida el conjunto final.
    String priority = request.getPriority() != null && !request.getPriority().isBlank()
        ? request.getPriority()
        : "Media";
    String status = request.getStatus() != null && !request.getStatus().isBlank()
        ? request.getStatus()
        : "Pendiente";
    ZoneId zoneId = timePolicy.resolve(request.getZoneId());
    timePolicy.requirePresentOrFuture(request.getEventDate(), zoneId);

    // 4. Crear modelo de dominio
    Event event = Event.create(
        request.getTitle(),
        request.getDescription(),
        priority,
        request.getEventDate(),
        request.getFrequency(),
        request.getReminder(),
        request.getStartHour(),
        request.getEndHour(),
        zoneId,
        status,
        categoryId,
        accountId);

    Event saved = eventRepository.save(event);

    // 5. Mapear respuesta
    return mapToResponse(saved, category);
  }

  /**
   * Convierte el modelo de dominio persistido en el DTO de respuesta,
   * incluyendo la categoría anidada si existe.
   *
   * @param event    evento persistido.
   * @param category categoría asociada (puede ser {@code null}).
   * @return {@link EventResponse} listo para ser retornado al cliente.
   */
  private EventResponse mapToResponse(Event event, Category category) {
    CategoryResponse categoryResponse = null;
    if (category != null) {
      categoryResponse = CategoryResponse.builder()
          .id(category.getId())
          .name(category.getName())
          .type(category.getType())
          .scope(category.getScope())
          .build();
    }

    return EventResponse.builder()
        .id(event.getId())
        .title(event.getTitle())
        .priority(event.getPriority())
        .eventDate(event.getDate())
        .zoneId(event.getZoneId().getId())
        .frequency(event.getFrequency())
        .reminder(event.getReminder())
        .reminderInstant(event.getReminderInstant())
        .startHour(event.getStartHour())
        .startInstant(event.getStartInstant())
        .endHour(event.getEndHour())
        .endInstant(event.getEndInstant())
        .description(event.getDescription())
        .status(event.getStatus())
        .category(categoryResponse)
        .build();
  }
}
