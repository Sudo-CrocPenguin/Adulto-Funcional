package org.adultofuncional.main.agenda.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta que expone los datos de un evento de la agenda.
 *
 * <p>
 * Proyecta la información completa de un evento hacia los clientes de la API
 * después de ejecutar operaciones de consulta, creación o actualización.
 * Incluye la categoría asociada como un objeto anidado
 * {@link CategoryResponse} cuando el evento tiene una categoría vinculada,
 * evitando que el cliente deba realizar consultas adicionales para obtener
 * sus datos.
 *
 * <p>
 * Nunca expone campos de infraestructura como marcas de auditoría ni
 * referencias internas a entidades JPA. Los datos retornados se limitan
 * a los atributos de negocio definidos en el modelo de dominio
 * {@link org.adultofuncional.main.agenda.domain.model.Event}.
 *
 * <h2>Campos expuestos</h2>
 * <ul>
 * <li>{@link #id} — identificador único (UUID v7).</li>
 * <li>{@link #title} — título del evento.</li>
 * <li>{@link #priority} — prioridad ({@code Baja}, {@code Media},
 * {@code Alta}).</li>
 * <li>{@link #eventDate} — fecha calendario del evento.</li>
 * <li>{@link #frequency} — frecuencia de repetición en días (0 = único).</li>
 * <li>{@link #reminder} — fecha y hora del recordatorio programado.</li>
 * <li>{@link #startHour} — hora de inicio.</li>
 * <li>{@link #endHour} — hora de finalización.</li>
 * <li>{@link #description} — descripción textual (opcional, puede ser
 * {@code null}).</li>
 * <li>{@link #status} — estado ({@code Pendiente}, {@code Completado},
 * {@code Cancelado}, {@code Pospuesto}).</li>
 * <li>{@link #category} — categoría asociada (puede ser {@code null}).</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase
 * @see org.adultofuncional.main.agenda.application.usecase.GetEventUseCase
 * @see org.adultofuncional.main.agenda.application.usecase.ListEventsUseCase
 * @see org.adultofuncional.main.agenda.application.usecase.UpdateEventUseCase
 * @see CategoryResponse
 */
@Getter
@Builder
public class EventResponse {

  /**
   * Identificador único del evento (UUID v7).
   * Generado por el dominio al crear el evento.
   */
  private UUID id;

  /**
   * Título del evento.
   * Ejemplos: "Reunión de equipo", "Cita médica", "Cumpleaños de Julia".
   */
  private String title;

  /**
   * Prioridad del evento.
   * Valores posibles: {@code "Baja"}, {@code "Media"}, {@code "Alta"}.
   * Por defecto {@code "Media"} si no se especificó en la creación.
   */
  private String priority;

  /**
   * Fecha calendario del evento.
   * Representa el día en que ocurre el evento, sin información de hora.
   */
  private LocalDate eventDate;

  /**
   * Frecuencia de repetición del evento en días.
   * {@code 0} indica evento único; valores positivos indican repetición
   * periódica (1 = diario, 7 = semanal, 30 = mensual, 365 = anual).
   */
  private Integer frequency;

  /**
   * Fecha y hora programada para el recordatorio del evento.
   */
  private LocalDateTime reminder;

  /**
   * Hora de inicio del evento.
   * Incluye fecha y hora para eventos que pueden extenderse por varios días.
   */
  private LocalDateTime startHour;

  /**
   * Hora de finalización del evento.
   * Siempre es posterior a {@link #startHour}.
   */
  private LocalDateTime endHour;

  /**
   * Descripción detallada del evento.
   * Campo opcional; {@code null} si no se proporcionó descripción.
   */
  private String description;

  /**
   * Estado actual del evento.
   * Valores posibles: {@code "Pendiente"}, {@code "Completado"},
   * {@code "Cancelado"}, {@code "Pospuesto"}.
   * Por defecto {@code "Pendiente"} si no se especificó en la creación.
   */
  private String status;

  /**
   * Categoría financiera asociada al evento.
   * Contiene los datos completos de la categoría como un
   * {@link CategoryResponse} anidado.
   * Es {@code null} si el evento no tiene categoría asociada.
   */
  private CategoryResponse category;
}
