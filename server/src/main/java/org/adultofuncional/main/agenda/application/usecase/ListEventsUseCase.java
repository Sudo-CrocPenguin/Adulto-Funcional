package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar eventos de la agenda con filtros opcionales.
 *
 * <p>
 * Recupera todos los eventos de una cuenta desde
 * {@link EventRepository#findAllByAccountId} y aplica en memoria los filtros
 * proporcionados: estado, prioridad y categoría. Las categorías asociadas se
 * cargan en un único lote a través de
 * {@link CategoryRepository#findAllById(Iterable)} para evitar consultas N+1.
 *
 * <p>
 * <strong>Filtros soportados (todos opcionales):</strong>
 * <ul>
 * <li>{@code status} — filtra por estado del evento (ej.
 * {@code "Pendiente"}).</li>
 * <li>{@code priority} — filtra por prioridad (ej. {@code "Alta"}).</li>
 * <li>{@code categoryId} — filtra por categoría asociada.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see EventRepository
 * @see CategoryRepository
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class ListEventsUseCase {

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;

  // TODO: Agregar paginación y filtros por rango de fechas
  // TODO: Permitir ordenamiento por fecha, prioridad, etc.

  /**
   * Ejecuta el listado filtrado de eventos.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param status     Estado a filtrar (opcional). Puede ser {@code null} o
   *                   vacío.
   * @param priority   Prioridad a filtrar (opcional). Puede ser {@code null}
   *                   o vacío.
   * @param categoryId Categoría a filtrar (opcional).
   * @return Lista de {@link EventResponse} con los eventos que cumplen los
   *         criterios, incluyendo la categoría anidada si existe.
   */
  @Transactional(readOnly = true)
  public List<EventResponse> execute(UUID accountId, String status, String priority, UUID categoryId) {
    List<Event> events = eventRepository.findAllByAccountId(accountId);

    if (status != null && !status.isBlank()) {
      events = events.stream()
          .filter(e -> e.getStatus().equals(status))
          .collect(Collectors.toList());
    }
    if (priority != null && !priority.isBlank()) {
      events = events.stream()
          .filter(e -> e.getPriority().equals(priority))
          .collect(Collectors.toList());
    }
    if (categoryId != null) {
      events = events.stream()
          .filter(e -> e.getCategoryId() != null && e.getCategoryId().equals(categoryId))
          .collect(Collectors.toList());
    }

    // Cargar categorías en lote para evitar N+1
    Set<UUID> categoryIds = events.stream()
        .map(Event::getCategoryId)
        .collect(Collectors.toSet());
    Map<UUID, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));

    return events.stream()
        .map(event -> {
          Category cat = categoryMap.get(event.getCategoryId());
          CategoryResponse categoryResponse = null;
          if (cat != null) {
            categoryResponse = CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .type(cat.getType())
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
        })
        .collect(Collectors.toList());
  }
}
