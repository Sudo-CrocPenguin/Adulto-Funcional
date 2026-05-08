package org.adultofuncional.main.agenda.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para crear un nuevo evento
 * (compromiso) en la agenda personal.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code title} — obligatorio, máximo 35 caracteres, sin HTML.</li>
 * <li>{@code priority} — opcional, máximo 15 caracteres. Si no se envía o está
 * en blanco, el caso de uso asigna {@code "Media"}.</li>
 * <li>{@code eventDate} — obligatorio, debe ser la fecha actual o futura.</li>
 * <li>{@code frequency} — obligatorio, debe ser un entero no negativo
 * (0 = único, 1 = diario, 7 = semanal, 30 = mensual, 365 = anual).</li>
 * <li>{@code reminder} — obligatorio, fecha y hora del recordatorio.</li>
 * <li>{@code startHour} — obligatorio, hora de inicio del evento.</li>
 * <li>{@code endHour} — obligatorio, hora de finalización. El caso de uso
 * valida que sea posterior a {@code startHour}.</li>
 * <li>{@code description} — opcional, máximo 65,535 caracteres (tamaño
 * {@code TEXT} en base de datos), sin HTML.</li>
 * <li>{@code status} — opcional. Si no se envía o está en blanco, el caso de
 * uso asigna {@code "Pendiente"}.</li>
 * <li>{@code categoryId} — opcional, referencia a una categoría existente.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * Los campos de texto libre ({@code title} y {@code description}) están
 * anotados con {@link NoHtml}. Cualquier petición que contenga HTML
 * (ej. {@code <script>}, {@code <img onerror=...>}) será rechazada con un
 * error 400, evitando el almacenamiento de scripts maliciosos (Stored XSS).
 * La validación se basa en Jsoup con una {@code Safelist.none()}, es decir,
 * no se permite ningún tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class EventRequest {

  /**
   * Título del evento.
   * Obligatorio, máximo 35 caracteres, sin HTML.
   */
  @NotBlank(message = "El título es obligatorio")
  @Size(max = 35, message = "El título no puede exceder 35 caracteres")
  @NoHtml
  private String title;

  /**
   * Prioridad del evento.
   * Opcional, máximo 15 caracteres.
   *
   * <p>
   * Valores aceptados por el caso de uso: {@code "Baja"}, {@code "Media"},
   * {@code "Alta"}. Si no se proporciona, se asigna {@code "Media"}.
   */
  @Size(max = 15, message = "Prioridad no válida (Baja, Media, Alta)")
  private String priority;

  /**
   * Fecha calendario del evento.
   * Obligatorio, debe ser la fecha actual o una fecha futura.
   */
  @NotNull(message = "La fecha del evento es obligatoria")
  @FutureOrPresent(message = "La fecha no puede ser pasada")
  private LocalDate eventDate;

  /**
   * Frecuencia de repetición en días.
   * Obligatorio.
   *
   * <p>
   * Valores típicos: {@code 0} (evento único), {@code 1} (diario),
   * {@code 7} (semanal), {@code 30} (mensual), {@code 365} (anual).
   */
  @NotNull(message = "La frecuencia es obligatoria")
  private Integer frequency;

  /**
   * Fecha y hora del recordatorio programado.
   * Obligatorio.
   */
  @NotNull(message = "El recordatorio es obligatorio")
  private LocalDateTime reminder;

  /**
   * Hora de inicio del evento.
   * Obligatorio. Debe ser anterior a {@link #endHour}.
   */
  @NotNull(message = "La hora de inicio es obligatoria")
  private LocalDateTime startHour;

  /**
   * Hora de finalización del evento.
   * Obligatorio. Debe ser posterior a {@link #startHour}.
   */
  @NotNull(message = "La hora de fin es obligatoria")
  private LocalDateTime endHour;

  /**
   * Descripción detallada del evento.
   * Opcional, máximo 65,535 caracteres (tamaño {@code TEXT}), sin HTML.
   */
  @Size(max = 65535, message = "Descripción demasiado larga")
  @NoHtml
  private String description;

  /**
   * Estado del evento.
   * Opcional.
   *
   * <p>
   * Valores aceptados por el caso de uso: {@code "Pendiente"},
   * {@code "Completado"}, {@code "Cancelado"}, {@code "Pospuesto"}.
   * Si no se proporciona, se asigna {@code "Pendiente"}.
   */
  private String status;

  /**
   * Identificador de la categoría asociada al evento.
   * Opcional. Si es {@code null}, el evento se registra sin categoría.
   */
  private UUID categoryId;
}
