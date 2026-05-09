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
 * <p>
 * Registra un evento (compromiso) asociado a una cuenta y, opcionalmente, a
 * una categoría. Antes de persistir, verifica que la cuenta y la categoría
 * existan, que la hora de inicio sea estrictamente anterior a la de fin, y
 * asigna valores por defecto para prioridad y estado si no fueron
 * especificados.
 *
 * <p>
 * <strong>Reglas de negocio aplicadas:</strong>
 * <ul>
 * <li>La cuenta debe existir en el módulo de cuentas.</li>
 * <li>Si se proporciona {@code categoryId}, la categoría debe existir.</li>
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
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
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

  // TODO: Validar valores de frecuencia permitidos (0,1,7,30,365)
  // TODO: Validar que el recordatorio sea anterior a la fecha del evento

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
   * @throws BusinessException si las horas no son coherentes o si la prioridad
   *                           o el estado no son valores válidos.
   */
  @Transactional
  public EventResponse execute(UUID accountId, EventRequest request) {
    // 1. Verificar cuenta
    accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    // 2. Validar horario
    if (request.getStartHour().isAfter(request.getEndHour()) ||
        request.getStartHour().isEqual(request.getEndHour())) {
      throw new BusinessException("La hora de inicio debe ser anterior a la hora de fin");
    }

    // 3. Buscar categoría (opcional)
    Category category = null;
    UUID categoryId = null;
    if (request.getCategoryId() != null) {
      category = categoryRepository.findById(request.getCategoryId())
          .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: "
              + request.getCategoryId()));
      categoryId = category.getId();
    }

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

  /**
   * Valida que la prioridad sea uno de los valores permitidos.
   *
   * @param priority valor a validar.
   * @throws BusinessException si no es {@code Baja}, {@code Media} o
   *                           {@code Alta}.
   */
  private void validarPrioridad(String priority) {
    if (!priority.equals("Baja") && !priority.equals("Media") && !priority.equals("Alta")) {
      throw new BusinessException("La prioridad debe ser 'Baja', 'Media' o 'Alta'");
    }
  }

  /**
   * Valida que el estado sea uno de los valores permitidos.
   *
   * @param status valor a validar.
   * @throws BusinessException si no es un estado reconocido.
   */
  private void validarEstado(String status) {
    if (!status.equals("Pendiente") && !status.equals("Completado") &&
        !status.equals("Cancelado") && !status.equals("Pospuesto")) {
      throw new BusinessException("El estado debe ser 'Pendiente', 'Completado', 'Cancelado' o 'Pospuesto'");
    }
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
