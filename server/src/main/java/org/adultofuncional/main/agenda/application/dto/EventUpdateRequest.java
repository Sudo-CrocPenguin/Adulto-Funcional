package org.adultofuncional.main.agenda.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.adultofuncional.main.shared.security.NoHtml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para actualizar un evento existente (PATCH).
 *
 * <p><strong>¿Qué es?</strong><br>
 * Objeto que acepta campos opcionales para modificar un evento.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Permite actualizaciones parciales del título, prioridad, fecha, frecuencia,
 * recordatorio, horarios, descripción, estado o categoría.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class EventUpdateRequest {

    @Size(max = 35, message = "El título no puede exceder 35 caracteres")
    @NoHtml
    private String title;

    @Size(max = 15, message = "Prioridad no válida")
    private String priority;

    @FutureOrPresent(message = "La fecha no puede ser pasada")
    private LocalDate eventDate;

    private Integer frequency;

    private LocalDateTime reminder;

    private LocalDateTime startHour;

    private LocalDateTime endHour;

    @Size(max = 65535, message = "Descripción demasiado larga")
    @NoHtml
    private String description;

    private String status;

    private UUID categoryId;
}
