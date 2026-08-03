package org.adultofuncional.main.agenda.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Filtros y paginación del listado de eventos.
 *
 * <p><strong>Qué es:</strong> el contrato de query parameters del endpoint de
 * agenda.</p>
 *
 * <p><strong>Para qué sirve:</strong> combina estado, prioridad, categoría y
 * rango inclusivo de fechas con el contrato común del ADR 0005.</p>
 *
 * <p><strong>Cómo funciona:</strong> Spring enlaza los parámetros opcionales;
 * el caso de uso valida el rango y la lista blanca de orden antes de delegar
 * la página a persistencia.</p>
 */
@Getter
@Setter
public class EventFilterRequest {

  @Pattern(
      regexp = "^(Pendiente|Completado|Cancelado|Pospuesto)?$",
      message = "El estado no es válido")
  private String status;

  @Pattern(regexp = "^(Baja|Media|Alta)?$", message = "La prioridad no es válida")
  private String priority;

  private UUID categoryId;
  private LocalDate startDate;
  private LocalDate endDate;
  private String sortBy;
  private String sortDirection;
  private Integer page;
  private Integer size;
}
