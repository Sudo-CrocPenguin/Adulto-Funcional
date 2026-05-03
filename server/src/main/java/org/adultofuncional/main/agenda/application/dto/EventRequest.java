package org.adultofuncional.main.agenda.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la solicitud para crear o actualizar un evento.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula los datos que el cliente (frontend) envía al servidor
 * para crear un nuevo evento en la agenda o actualizar uno existente.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para recibir y validar la información de un evento (título, prioridad,
 * fecha, frecuencia, recordatorio, horario, descripción, categoría) antes de que
 * sea procesada por los casos de uso correspondientes.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * El controlador {@code AgendaController} recibe un objeto JSON en el cuerpo de la
 * petición POST a {@code /api/agenda/events}, lo deserializa a {@code EventRequest}
 * y aplica las validaciones de Bean Validation automáticamente.
 * Si pasa las validaciones, se pasa al caso de uso {@code CreateEventUseCase}.
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code title} - Título del evento (obligatorio, máx 35 caracteres)</li>
 *   <li>{@code priority} - Prioridad del evento (opcional: "Baja", "Media", "Alta")</li>
 *   <li>{@code eventDate} - Fecha calendario del evento (obligatorio)</li>
 *   <li>{@code frequency} - Frecuencia en días (0=único, 1=diario, 7=semanal, etc.)</li>
 *   <li>{@code reminder} - Fecha/hora del recordatorio (obligatorio)</li>
 *   <li>{@code startHour} - Hora de inicio del evento (obligatorio)</li>
 *   <li>{@code endHour} - Hora de finalización del evento (obligatorio)</li>
 *   <li>{@code description} - Descripción detallada (opcional)</li>
 *   <li>{@code status} - Estado del evento (opcional: "Pendiente", "Completado", etc.)</li>
 *   <li>{@code categoryId} - ID de la categoría asociada (opcional)</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase
 * @see EventResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    /**
     * Título del evento.
     * Obligatorio. Mínimo 1 carácter, máximo 35.
     */
    @NotBlank(message = "El título del evento es obligatorio")
    @Size(max = 35, message = "El título no puede exceder 35 caracteres")
    private String title;

    /**
     * Prioridad del evento.
     * Opcional. Valores permitidos: "Baja", "Media", "Alta".
     * Por defecto se asignará "Media" si no se proporciona.
     */
    @Size(max = 15, message = "La prioridad no puede exceder 15 caracteres")
    private String priority;

    /**
     * Fecha calendario del evento.
     * Obligatorio. No puede ser una fecha pasada (validación opcional).
     *
     */

    //TODO: Decidir si permitir fechas pasadas (eventos retrospectivos)
    //TODO: Considerar @FutureOrPresent para eventos futuros
    
    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate eventDate;

    /**
     * Frecuencia de repetición en días.
     * Obligatorio. Valor mínimo 0.
     * <ul>
     *   <li>{@code 0} → evento único (sin repetición)</li>
     *   <li>{@code 1} → diario</li>
     *   <li>{@code 7} → semanal</li>
     *   <li>{@code 30} → mensual</li>
     *   <li>{@code 365} → anual</li>
     * </ul>
     */

    //TODO: Validar valores de frecuencia permitidos (0, 1, 7, 30, 365)

    @NotNull(message = "La frecuencia es obligatoria")
    @Min(value = 0, message = "La frecuencia no puede ser negativa")
    private Integer frequency;

    /**
     * Fecha y hora del recordatorio.
     * Obligatorio. El sistema enviará una notificación en este momento.
     */
    @NotNull(message = "El recordatorio es obligatorio")
    private LocalDateTime reminder;

    /**
     * Hora de inicio del evento.
     * Obligatorio. Debe ser anterior a la hora de fin.
     */
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalDateTime startHour;

    /**
     * Hora de finalización del evento.
     * Obligatorio. Debe ser posterior a la hora de inicio.
     *
     */

    //TODO: Agregar validación @After para startHour < endHour

    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalDateTime endHour;

    /**
     * Descripción detallada del evento.
     * Opcional. Máximo 65535 caracteres (TEXT en MySQL).
     */
    @Size(max = 65535, message = "La descripción es demasiado larga")
    private String description;

    /**
     * Estado del evento.
     * Opcional. Valores permitidos: "Pendiente", "Completado", "Cancelado", "Pospuesto".
     * Por defecto se asignará "Pendiente" si no se proporciona.
     */
    @Size(max = 20, message = "El estado no puede exceder 20 caracteres")
    private String status;

    /**
     * ID de la categoría asociada al evento.
     * Opcional. Si se proporciona, debe existir una categoría con ese ID.
     */
    private UUID categoryId;
}