package org.adultofuncional.main.agenda.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.adultofuncional.main.shared.security.NoHtml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para crear un nuevo evento (compromiso) en la agenda.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Objeto que recibe los datos para registrar un nuevo evento.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Valida que el título, fecha, prioridad, frecuencia, recordatorio y horario
 * sean correctos antes de crear el evento.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Se envía en el cuerpo de la petición POST a {@code /api/agenda/events}.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class EventRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 35, message = "El título no puede exceder 35 caracteres")
    @NoHtml
    private String title;

    @Size(max = 15, message = "Prioridad no válida (Baja, Media, Alta)")
    private String priority; // Baja, Media, Alta

    @NotNull(message = "La fecha del evento es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser pasada")
    private LocalDate eventDate;

    @NotNull(message = "La frecuencia es obligatoria")
    private Integer frequency; // 0=único, 1=diario, 7=semanal, 30=mensual, 365=anual

    @NotNull(message = "El recordatorio es obligatorio")
    private LocalDateTime reminder;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalDateTime startHour;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalDateTime endHour;

    @Size(max = 65535, message = "Descripción demasiado larga")
    @NoHtml
    private String description;

    private String status; // Pendiente, Completado, Cancelado, Pospuesto

    private UUID categoryId;
}
