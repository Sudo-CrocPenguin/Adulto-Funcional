package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para crear
 * un nuevo gasto fijo recurrente.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code name} — obligatorio, máximo 20 caracteres.</li>
 * <li>{@code frequency} — obligatorio, debe ser un valor válido de
 * {@link Frequency}.</li>
 * <li>{@code amount} — obligatorio, desde 0.01 hasta 99,999,999.99 y con
 * máximo dos decimales.</li>
 * <li>{@code status} — obligatorio, debe ser un valor válido de
 * {@link Status}.</li>
 * <li>{@code nextDueDate} — obligatorio, debe ser una fecha futura.</li>
 * <li>{@code categoryId} — obligatorio, referencia a una categoría
 * existente.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code name} está anotado con {@link NoHtml}.
 * Cualquier petición que contenga HTML (ej. {@code <script>},
 * {@code <img onerror=...>}) será rechazada con un error 400, evitando
 * el almacenamiento de scripts maliciosos en la base de datos (Stored XSS).
 * La validación se basa en Jsoup con una {@code Safelist.none()},
 * es decir, no se permite ningún tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.fixedexpense.CreateFixedExpenseUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class CreateFixedExpenseRequest {

  /**
   * Nombre descriptivo del gasto fijo.
   *
   * <p>
   * Identifica de forma legible el gasto recurrente dentro del sistema
   * (por ejemplo: "Netflix", "Arriendo", "Gimnasio").
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotBlank}: el nombre no puede ser nulo, vacío ni contener
   * únicamente espacios en blanco.</li>
   * <li>{@code @Size(max = 20)}: el nombre no puede superar los 20
   * caracteres.</li>
   * <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML, previniendo
   * ataques de inyección de código en este campo.</li>
   * </ul>
   */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
  @NoHtml
  private String name;

  /**
   * Frecuencia de cobro o recurrencia del gasto fijo.
   *
   * <p>
   * Campo obligatorio que define cada cuánto tiempo se genera el gasto,
   * según el enumerado {@link Frequency}
   * (por ejemplo: diario, semanal, mensual, anual, entre otros).
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: la frecuencia no puede ser nula; debe proporcionarse
   * un valor válido del enumerado {@link Frequency}.</li>
   * </ul>
   */
  @NotNull(message = "La frecuencia es obligatoria")
  private Frequency frequency;

  /**
   * Monto monetario del gasto fijo.
   *
   * <p>
   * Campo obligatorio que representa el valor económico del gasto recurrente.
   * Se utiliza {@link BigDecimal} para garantizar precisión en los cálculos
   * monetarios y evitar errores de redondeo propios de tipos flotantes.
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: el monto no puede ser nulo.</li>
   * <li>{@code @DecimalMin("0.01")}: el monto debe ser mayor a cero,
   * garantizando que no se registren gastos sin valor económico.</li>
   * <li>{@code @Digits}: limita el valor a la precisión {@code DECIMAL(10,2)}
   * utilizada por MariaDB.</li>
   * </ul>
   */
  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
  @Digits(integer = 8, fraction = 2, message = "El monto admite máximo 8 enteros y 2 decimales")
  private BigDecimal amount;

  /**
   * Estado actual del gasto fijo dentro del sistema.
   *
   * <p>
   * Campo obligatorio que indica la situación operativa del gasto recurrente
   * según el enumerado {@link Status}
   * (por ejemplo: activo, pausado, cancelado, entre otros).
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: el estado no puede ser nulo; debe proporcionarse
   * un valor válido del enumerado {@link Status}.</li>
   * </ul>
   */
  @NotNull(message = "El estado es obligatorio")
  private Status status;

  /** Fecha de inicio del ciclo; si se omite se usa el día actual del reloj. */
  private LocalDate startDate;

  /** Días de anticipación del recordatorio; cero conserva la compatibilidad. */
  @Min(value = 0, message = "Los días de recordatorio no pueden ser negativos")
  @Max(value = 3650, message = "Los días de recordatorio no pueden exceder 3650")
  private Integer reminderDays;

  /**
   * Próxima fecha de vencimiento del gasto fijo.
   *
   * <p>
   * Campo obligatorio que indica el siguiente vencimiento del ciclo. El caso
   * de uso exige que sea posterior al día actual y el dominio también exige
   * coherencia con {@code startDate}.
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: el próximo vencimiento no puede ser nulo.</li>
   * <li>La política basada en {@code Clock} exige una fecha futura.</li>
   * </ul>
   */
  @NotNull(message = "La fecha de cierre es obligatoria")
  private LocalDate nextDueDate;

  /**
   * Identificador de la categoría financiera asociada al gasto fijo.
   *
   * <p>
   * Campo obligatorio que vincula el gasto fijo con una categoría
   * existente en el sistema para facilitar su clasificación y análisis.
   */
  @NotNull(message = "La categoria es obligatoria")
  private UUID categoryId;
}
