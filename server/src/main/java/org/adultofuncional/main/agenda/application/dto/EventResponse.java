package org.adultofuncional.main.agenda.application.dto;

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
public class EventResponse {

    private UUID id;

    private String title;

    private String priority;

    private LocalDate eventDate;

    private Integer frequency;

    private LocalDateTime reminder;

    private LocalDateTime startHour;

    private LocalDateTime endHour;

    private String description;

    private String status;

    private EventCategoryDTO category;

    // TODO: Agregar campo para indicar si es evento recurrente
    // TODO: Agregar campo para próxima ocurrencia (para eventos con frecuencia)
}