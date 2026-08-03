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
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para modificar
 * un gasto fijo existente.
 *
 * <p>
 * A diferencia del DTO de creación, todos los campos son opcionales:
 * el cliente puede enviar únicamente aquellos que desea modificar,
 * habilitando actualizaciones parciales (comportamiento PATCH).
 * Los campos no incluidos (nulos) son ignorados por la capa de aplicación
 * y el valor actual del dominio se mantiene sin cambios.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code name} — opcional, máximo 20 caracteres si se proporciona.</li>
 * <li>{@code frequency} — opcional, debe ser un valor válido de
 * {@link Frequency} si se proporciona.</li>
 * <li>{@code amount} — opcional, debe ser mayor a 0.01 si se proporciona.</li>
 * <li>{@code status} — opcional, debe ser un valor válido de
 * {@link Status} si se proporciona.</li>
 * <li>{@code nextDueDate} — opcional, debe ser una fecha futura si se
 * proporciona.</li>
 * <li>{@code categoryId} — opcional, referencia a una categoría existente
 * si se proporciona.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code name} está anotado con {@link NoHtml}.
 * Cualquier valor que contenga HTML (ej. {@code <script>},
 * {@code <img onerror=...>}) será rechazado con un error 400, evitando
 * la persistencia de scripts maliciosos (Stored XSS). La validación se basa
 * en Jsoup con una {@code Safelist.none()}, es decir, no se permite ningún
 * tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.fixedexpense.UpdateFixedExpenseUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class UpdateFixedExpenseRequest {

  /**
   * Nuevo nombre descriptivo que se desea asignar al gasto fijo.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza el nombre actual
   * del gasto fijo. Si es {@code null}, el nombre permanece sin cambios.
   *
   * <p>
   * <b>Restricciones aplicadas cuando el valor es proporcionado:</b>
   * <ul>
   * <li>{@code @Size(max = 20)}: el nombre no puede superar los 20
   * caracteres.</li>
   * <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML, previniendo
   * ataques de inyección de código en este campo.</li>
   * </ul>
   */
  @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
  @NoHtml
  private String name;

  /**
   * Nueva frecuencia de cobro o recurrencia que se desea asignar al gasto fijo.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza la frecuencia actual
   * según el enumerado {@link Frequency}
   * (por ejemplo: diario, semanal, mensual, anual, entre otros).
   * Si es {@code null}, la frecuencia del gasto fijo permanece sin cambios.
   */
  private Frequency frequency;

  /**
   * Nuevo monto monetario que se desea asignar al gasto fijo.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza el monto actual del gasto
   * recurrente. Se utiliza {@link BigDecimal} para garantizar precisión en
   * los cálculos monetarios y evitar errores de redondeo propios de tipos
   * flotantes.
   * Si es {@code null}, el monto permanece sin cambios.
   *
   * <p>
   * <b>Restricciones aplicadas cuando el valor es proporcionado:</b>
   * <ul>
   * <li>{@code @DecimalMin("0.01")}: el monto debe ser mayor a cero,
   * garantizando que no se registren gastos sin valor económico.</li>
   * <li>{@code @Digits}: admite hasta ocho enteros y dos decimales, en
   * correspondencia con {@code DECIMAL(10,2)}.</li>
   * </ul>
   */
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
  @Digits(integer = 8, fraction = 2, message = "El monto admite máximo 8 enteros y 2 decimales")
  private BigDecimal amount;

  /**
   * Nuevo estado operativo que se desea asignar al gasto fijo.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza el estado actual
   * del gasto recurrente según el enumerado {@link Status}
   * (por ejemplo: activo, pausado, cancelado, entre otros).
   * Si es {@code null}, el estado del gasto fijo permanece sin cambios.
   */
  private Status status;

  /** Nueva fecha de inicio del ciclo recurrente. */
  private LocalDate startDate;

  /** Nueva anticipación del recordatorio, expresada en días. */
  @Min(value = 0, message = "Los días de recordatorio no pueden ser negativos")
  @Max(value = 3650, message = "Los días de recordatorio no pueden exceder 3650")
  private Integer reminderDays;

  /**
   * Nueva fecha de próximo vencimiento que se desea asignar al gasto fijo.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza el próximo vencimiento
   * del gasto recurrente. Se representa como {@link LocalDate} sin información
   * de hora ni zona horaria, dado que el ciclo opera a nivel de día
   * calendario.
   * Si es {@code null}, el vencimiento permanece sin cambios.
   *
   * El dominio valida que no sea anterior a la fecha de inicio final.
   */
  private LocalDate nextDueDate;

  /**
   * Identificador de la nueva categoría financiera que se desea asociar
   * al gasto fijo.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza la categoría actualmente
   * asociada al gasto fijo por aquella cuyo UUID corresponda al valor enviado.
   * Si es {@code null}, la categoría del gasto fijo permanece sin cambios.
   */
  private UUID categoryId;
}
