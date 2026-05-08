package org.adultofuncional.main.agenda.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para modificar
 * un evento existente en la agenda (actualización parcial).
 *
 * <p>
 * A diferencia del DTO de creación, todos los campos son opcionales:
 * el cliente puede enviar únicamente aquellos que desea modificar,
 * habilitando actualizaciones selectivas (comportamiento PATCH).
 * Los campos no incluidos (nulos) son ignorados por la capa de aplicación
 * y el valor actual del dominio se mantiene sin cambios.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code title} — opcional, máximo 35 caracteres si se proporciona.</li>
 * <li>{@code priority} — opcional, máximo 15 caracteres si se proporciona.</li>
 * <li>{@code eventDate} — opcional, debe ser presente o futura si se
 * proporciona.</li>
 * <li>{@code frequency} — opcional.</li>
 * <li>{@code reminder} — opcional.</li>
 * <li>{@code startHour} — opcional.</li>
 * <li>{@code endHour} — opcional.</li>
 * <li>{@code description} — opcional, máximo 65,535 caracteres si se
 * proporciona.</li>
 * <li>{@code status} — opcional.</li>
 * <li>{@code categoryId} — opcional, referencia a una categoría existente.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * Los campos de texto libre ({@code title} y {@code description}) están
 * anotados con {@link NoHtml}. Cualquier valor que contenga HTML
 * (ej. {@code <script>}, {@code <img onerror=...>}) será rechazado con un
 * error 400, evitando la persistencia de scripts maliciosos (Stored XSS).
 * La validación se basa en Jsoup con una {@code Safelist.none()}.
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase.UpdateEventUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class EventUpdateRequest {

  /**
   * Nuevo título para el evento.
   * Opcional, máximo 35 caracteres, sin HTML.
   */
  @Size(max = 35, message = "El título no puede exceder 35 caracteres")
  @NoHtml
  private String title;

  /**
   * Nueva prioridad para el evento.
   * Opcional, máximo 15 caracteres.
   *
   * <p>
   * Valores aceptados: {@code "Baja"}, {@code "Media"}, {@code "Alta"}.
   */
  @Size(max = 15, message = "Prioridad no válida")
  private String priority;

  /**
   * Nueva fecha calendario del evento.
   * Opcional, debe ser presente o futura si se proporciona.
   */
  @FutureOrPresent(message = "La fecha no puede ser pasada")
  private LocalDate eventDate;

  /**
   * Nueva frecuencia de repetición en días.
   * Opcional (0 = único, 1 = diario, 7 = semanal, etc.).
   */
  private Integer frequency;

  /**
   * Nueva fecha y hora del recordatorio.
   * Opcional.
   */
  private LocalDateTime reminder;

  /**
   * Nueva hora de inicio del evento.
   * Opcional.
   */
  private LocalDateTime startHour;

  /**
   * Nueva hora de finalización del evento.
   * Opcional.
   */
  private LocalDateTime endHour;

  /**
   * Nueva descripción del evento.
   * Opcional, máximo 65,535 caracteres, sin HTML.
   */
  @Size(max = 65535, message = "Descripción demasiado larga")
  @NoHtml
  private String description;

  /**
   * Nuevo estado del evento.
   * Opcional.
   *
   * <p>
   * Valores aceptados: {@code "Pendiente"}, {@code "Completado"},
   * {@code "Cancelado"}, {@code "Pospuesto"}.
   */
  private String status;

  /**
   * Identificador de la nueva categoría a asociar al evento.
   * Opcional.
   */
  private UUID categoryId;
}
