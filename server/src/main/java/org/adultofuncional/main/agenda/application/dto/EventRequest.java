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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "El título del evento es obligatorio")
    @Size(max = 35, message = "El título no puede exceder 35 caracteres")
    private String title;

    @Size(max = 15, message = "La prioridad no puede exceder 15 caracteres")
    private String priority;

    //TODO: Decidir si permitir fechas pasadas (eventos retrospectivos)
    //TODO: Considerar @FutureOrPresent para eventos futuros
     

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate eventDate;

    //TODO: Validar valores de frecuencia permitidos (0, 1, 7, 30, 365)

    @NotNull(message = "La frecuencia es obligatoria")
    @Min(value = 0, message = "La frecuencia no puede ser negativa")
    private Integer frequency;

    @NotNull(message = "El recordatorio es obligatorio")
    private LocalDateTime reminder;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalDateTime startHour;


     //TODO: Agregar validación @After para startHour < endHour

    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalDateTime endHour;

    @Size(max = 65535, message = "La descripción es demasiado larga")
    private String description;

    @Size(max = 20, message = "El estado no puede exceder 20 caracteres")
    private String status;

    private UUID categoryId;
}