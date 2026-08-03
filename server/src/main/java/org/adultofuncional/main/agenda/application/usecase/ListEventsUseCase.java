package org.adultofuncional.main.agenda.application.usecase;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.agenda.application.dto.EventResponse;
import org.adultofuncional.main.agenda.application.dto.EventFilterRequest;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.domain.repository.EventRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.pagination.PageQuery;
import org.adultofuncional.main.shared.pagination.PageResult;
import org.adultofuncional.main.shared.pagination.PaginationPolicy;
import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar eventos de la agenda con filtros opcionales.
 *
 * <p>
 * Delega ownership, filtros, orden y límites a una consulta SQL paginada. Las
 * categorías accesibles de la página se proyectan en un único lote.
 *
 * <p>
 * <strong>Filtros soportados (todos opcionales):</strong>
 * <ul>
 * <li>{@code status} — filtra por estado del evento (ej.
 * {@code "Pendiente"}).</li>
 * <li>{@code priority} — filtra por prioridad (ej. {@code "Alta"}).</li>
   * <li>{@code categoryId} — filtra por categoría asociada.</li>
 * <li>{@code startDate} / {@code endDate} — rango inclusivo de fechas.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see EventRepository
 * @see CategoryRepository
 * @see EventResponse
 */
@Service
@RequiredArgsConstructor
public class ListEventsUseCase {

  private static final Set<String> ALLOWED_SORTS =
      Set.of("eventDate", "startHour", "priority", "status", "title", "id");

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Ejecuta el listado filtrado de eventos.
   *
   * @param accountId  Identificador de la cuenta propietaria.
   * @param filter filtros y paginación opcionales
   * @return página de {@link EventResponse} con categoría y metadatos
   */
  @Transactional(readOnly = true)
  public PageResult<EventResponse> execute(UUID accountId, EventFilterRequest filter) {
    if (filter != null && filter.getStartDate() != null && filter.getEndDate() != null
        && filter.getStartDate().isAfter(filter.getEndDate())) {
      throw new BusinessException(
          "La fecha inicial no puede ser posterior a la fecha final",
          400,
          ApiErrorCode.PARAMETER_INVALID);
    }

    PageQuery pageQuery = PaginationPolicy.resolve(
        filter == null ? null : filter.getPage(),
        filter == null ? null : filter.getSize(),
        filter == null ? null : filter.getSortBy(),
        filter == null ? null : filter.getSortDirection(),
        "startHour",
        true,
        ALLOWED_SORTS);
    PageResult<Event> events = eventRepository.findPageByAccountId(
        accountId,
        filter == null || filter.getStatus() == null || filter.getStatus().isBlank()
            ? null : filter.getStatus(),
        filter == null || filter.getPriority() == null || filter.getPriority().isBlank()
            ? null : filter.getPriority(),
        filter == null ? null : filter.getCategoryId(),
        filter == null ? null : filter.getStartDate(),
        filter == null ? null : filter.getEndDate(),
        pageQuery);

    // Cargar categorías en lote para evitar N+1
    Set<UUID> categoryIds = events.content().stream()
        .map(Event::getCategoryId)
        .collect(Collectors.toSet());
    Map<UUID, Category> categoryMap = categoryRepository
        .findAllAccessibleById(accountId, categoryIds).stream()
        .collect(Collectors.toMap(Category::getId, Function.identity()));

    return events.map(event -> {
          Category cat = categoryMap.get(event.getCategoryId());
          CategoryResponse categoryResponse = null;
          if (cat != null) {
            categoryResponse = CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .type(cat.getType())
                .scope(cat.getScope())
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
        });
  }
}
