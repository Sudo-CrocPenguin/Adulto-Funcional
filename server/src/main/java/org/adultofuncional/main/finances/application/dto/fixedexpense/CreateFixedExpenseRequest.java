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
 * DTO (Data Transfer Object) que representa la solicitud de creación
 * de un nuevo gasto fijo en el sistema financiero.
 *
 * <p>
 * Esta clase encapsula y valida todos los datos que el cliente debe
 * enviar para registrar un gasto fijo recurrente. Actúa como contrato
 * de entrada en el caso de uso de creación de gastos fijos.
 * </p>
 *
 * <p>
 * <b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa y valida
 * los campos requeridos para crear un gasto fijo, aplicando restricciones
 * de validación mediante anotaciones de Bean Validation (Jakarta),
 * seguridad de contenido y reglas de negocio como montos mínimos
 * y fechas futuras.
 * </p>
 *
 * <p>
 * <b>¿Para qué sirve?</b><br>
 * Transporta desde el cliente hacia la capa de aplicación los datos
 * necesarios para registrar un gasto fijo recurrente: su nombre,
 * frecuencia de cobro, monto, estado, fecha de cierre y categoría
 * asociada opcional. Garantiza que los datos lleguen correctamente
 * formateados y validados antes de ser procesados por el sistema.
 * </p>
 *
 * <p>
 * <b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok
 * ({@code @Builder}),
 * y sus campos son accesibles a través de los getters generados por
 * {@code @Getter}.
 * Las anotaciones de validación de Jakarta Bean Validation son procesadas
 * automáticamente por el framework (Spring) al recibir la solicitud,
 * rechazando peticiones inválidas antes de llegar al caso de uso.
 * La anotación {@code @NoHtml} aplica una validación de seguridad personalizada
 * que previene la inyección de etiquetas HTML en los campos de texto.
 * </p>
 *
 * <p>
 * <b>Ejemplo de uso:</b>
 * </p>
 * 
 * <pre>{@code
 * CreateFixedExpenseRequest solicitud = CreateFixedExpenseRequest.builder()
 *     .name("Netflix")
 *     .frequency(Frequency.MONTHLY)
 *     .amount(new BigDecimal("15.99"))
 *     .status(Status.ACTIVE)
 *     .closingDate(LocalDate.of(2025, 12, 31))
 *     .categoryId(UUID.fromString("..."))
 *     .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
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
   * </p>
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * </p>
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
   * </p>
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * </p>
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
   * </p>
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * </p>
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
   * </p>
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * </p>
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
   * </p>
   *
   * <p>
   * <b>Restricciones aplicadas:</b>
   * </p>
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
   * </p>
   */
  @NotNull(message = "La categoria es obligatoria")
  private UUID categoryId;
}
