package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
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
 * <li>{@code amount} — obligatorio, debe ser mayor a 0.01.</li>
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
 * @see org.adultofuncional.main.finances.application.usecase.CreateFixedExpenseUseCase
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
   * </ul>
   */
  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
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

  /**
   * Fecha de cierre o vencimiento del gasto fijo.
   *
   * <p>
   * Campo obligatorio que indica hasta cuándo estará vigente el gasto
   * recurrente. Debe ser una fecha posterior a la actual, garantizando
   * que no se registren gastos con vigencia ya vencida.
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: la fecha de cierre no puede ser nula.</li>
   * <li>{@code @Future}: la fecha debe ser estrictamente posterior
   * a la fecha actual en el momento de la solicitud.</li>
   * </ul>
   */
  @NotNull(message = "La fecha de cierre es obligatoria")
  @Future(message = "La fecha de cierre debe ser futura")
  private LocalDate nextDueDate;

  /**
   * Identificador de la categoría financiera asociada al gasto fijo.
   *
   * <p>
   * Campo opcional que permite vincular el gasto fijo con una categoría
   * existente en el sistema para facilitar su clasificación y análisis.
   * Si es {@code null}, el gasto fijo se registra sin categoría asociada.
   */
  @NotNull(message = "La categoria es obligatoria")
  private UUID categoryId;
}
