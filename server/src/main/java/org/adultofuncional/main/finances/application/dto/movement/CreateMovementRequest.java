package org.adultofuncional.main.finances.application.dto.movement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para crear
 * un nuevo movimiento financiero (ingreso o egreso).
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code movementType} — obligatorio, debe ser {@code INCOME} o
 * {@code EXPENSE}.</li>
 * <li>{@code amount} — obligatorio, debe ser mayor a 0.01.</li>
 * <li>{@code movementDate} — obligatorio, fecha en la que ocurrió el
 * movimiento.</li>
 * <li>{@code description} — opcional, máximo 65,535 caracteres.</li>
 * <li>{@code categoryId} — opcional, referencia a una categoría existente.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code description} está anotado con {@link NoHtml}.
 * Cualquier petición que contenga HTML (ej. {@code <script>},
 * {@code <img onerror=...>}) será rechazada con un error 400, evitando
 * el almacenamiento de scripts maliciosos en la base de datos (Stored XSS).
 * La validación se basa en Jsoup con una {@code Safelist.none()},
 * es decir, no se permite ningún tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.CreateMovementUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class CreateMovementRequest {

  /**
   * Tipo de movimiento financiero a registrar.
   *
   * <p>
   * Campo obligatorio que clasifica el movimiento dentro del sistema
   * según el enumerado {@link MovementType}, determinando si corresponde
   * a un ingreso o a un egreso en las finanzas del usuario.
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: el tipo de movimiento no puede ser nulo; debe
   * proporcionarse un valor válido del enumerado {@link MovementType}.</li>
   * </ul>
   */
  @NotNull(message = "El tipo de movimiento es obligatorio")
  private MovementType movementType;

  /**
   * Monto monetario del movimiento financiero.
   *
   * <p>
   * Campo obligatorio que representa el valor económico del movimiento.
   * Se utiliza {@link BigDecimal} para garantizar precisión en los cálculos
   * monetarios y evitar errores de redondeo propios de tipos flotantes.
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: el monto no puede ser nulo.</li>
   * <li>{@code @DecimalMin("0.01")}: el monto debe ser mayor a cero,
   * garantizando que no se registren movimientos sin valor económico.</li>
   * </ul>
   */
  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
  private BigDecimal amount;

  /**
   * Fecha en la que ocurrió el movimiento financiero.
   *
   * <p>
   * Campo obligatorio que indica el día calendario en que se realizó
   * el ingreso o egreso. Se representa como {@link LocalDate} sin información
   * de hora ni zona horaria, dado que el registro opera a nivel de día
   * calendario.
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * <ul>
   * <li>{@code @NotNull}: la fecha del movimiento no puede ser nula.</li>
   * </ul>
   */
  @NotNull(message = "La fecha es obligatoria")
  private LocalDate movementDate;

  /**
   * Descripción textual opcional del movimiento financiero.
   *
   * <p>
   * Campo opcional que permite al usuario agregar detalles o notas
   * sobre el movimiento registrado (por ejemplo: "Compra supermercado",
   * "Pago de servicios", "Transferencia recibida"). Si es {@code null},
   * el movimiento se registra sin descripción.
   * 
   *
   * <p>
   * <b>Restricciones aplicadas cuando el valor es proporcionado:</b>
   * <ul>
   * <li>{@code @Size(max = 65535)}: la descripción no puede superar los
   * 65,535 caracteres, límite que corresponde al tipo de dato {@code TEXT}
   * en base de datos.</li>
   * <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML,
   * previniendo ataques de inyección de código en este campo.</li>
   * </ul>
   */
  @Size(max = 65535, message = "La descripción es demasiado larga")
  @NoHtml
  private String description;

  /**
   * Identificador de la categoría financiera asociada al movimiento.
   *
   * <p>
   * Campo opcional que permite vincular el movimiento con una categoría
   * existente en el sistema para facilitar su clasificación y análisis.
   * Corresponde al UUID único de la categoría registrada.
   * Si es {@code null}, el movimiento se registra sin categoría asociada.
   */
  private UUID categoryId;
}
