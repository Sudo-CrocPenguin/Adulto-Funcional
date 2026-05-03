package org.adultofuncional.main.agenda.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de un evento de agenda.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula la información de un evento que se envía al cliente
 * (frontend) como respuesta a una operación de consulta o creación.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para devolver al usuario la información de sus eventos de agenda de forma
 * segura y estructurada, excluyendo datos sensibles de la cuenta propietaria.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Un caso de uso (ej. {@code CreateEventUseCase} o {@code ListEventsUseCase})
 * recupera una entidad {@code EventEntity} del repositorio, la mapea a este DTO
 * y la retorna al controlador. El controlador la serializa a JSON y la envía como respuesta.
 *
 * <p><strong>Frecuencia:</strong>
 * <ul>
 *   <li>{@code 0} → evento único</li>
 *   <li>{@code 1} → diario</li>
 *   <li>{@code 7} → semanal</li>
 *   <li>{@code 30} → mensual</li>
 *   <li>{@code 365} → anual</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase
 * @see org.adultofuncional.main.agenda.application.usecase.ListEventsUseCase
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    /**
     * Identificador único del evento (UUID v7).
     */
    private UUID id;

    /**
     * Título del evento.
     */
    private String title;

    /**
     * Prioridad del evento: "Baja", "Media" o "Alta".
     */
    private String priority;

    /**
     * Fecha calendario del evento.
     */
    private LocalDate eventDate;

    /**
     * Frecuencia de repetición en días.
     * 0 = único, 1 = diario, 7 = semanal, 30 = mensual, 365 = anual.
     */
    private Integer frequency;

    /**
     * Fecha y hora del recordatorio.
     * El sistema enviará una notificación en este momento.
     */
    private LocalDateTime reminder;

    /**
     * Hora de inicio del evento.
     */
    private LocalDateTime startHour;

    /**
     * Hora de finalización del evento.
     */
    private LocalDateTime endHour;

    /**
     * Descripción detallada del evento (opcional).
     */
    private String description;

    /**
     * Estado del evento: "Pendiente", "Completado", "Cancelado" o "Pospuesto".
     */
    private String status;

    /**
     * Categoría asociada al evento (puede ser null).
     */
    private EventCategoryDTO category;

    // TODO: Agregar campo para indicar si es evento recurrente
    // TODO: Agregar campo para próxima ocurrencia (para eventos con frecuencia)
}