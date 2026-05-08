package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa la solicitud de actualización
 * de un gasto fijo existente en el sistema financiero.
 *
 * <p>Esta clase encapsula los datos que el cliente puede enviar para modificar
 * parcialmente un gasto fijo ya registrado. A diferencia del DTO de creación,
 * todos sus campos son opcionales, permitiendo actualizaciones parciales
 * donde solo se envían los campos que se desean modificar.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa los campos
 * modificables de un gasto fijo, aplicando validaciones de formato,
 * seguridad y reglas de negocio únicamente sobre los campos que
 * lo requieren, sin forzar la presencia de ninguno.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Transporta desde el cliente hacia la capa de aplicación los datos
 * que se desean actualizar sobre un gasto fijo existente: nombre,
 * frecuencia, monto, estado, fecha de cierre y categoría asociada,
 * o cualquier combinación de ellos. Al no tener restricciones
 * {@code @NotNull} ni {@code @NotBlank}, el sistema interpreta los
 * campos no enviados como sin cambios, habilitando actualizaciones
 * parciales (PATCH).</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * y sus campos son accesibles a través de los getters generados por {@code @Getter}.
 * Cuando el framework (Spring) recibe la solicitud, aplica las validaciones
 * activas sobre los campos proporcionados: {@code @Size} y {@code @NoHtml}
 * protegen el nombre, {@code @DecimalMin} garantiza un monto válido si se
 * envía, y {@code @Future} asegura que la fecha de cierre, si se proporciona,
 * sea posterior a la fecha actual. La capa de aplicación es responsable
 * de ignorar los campos nulos y aplicar solo los cambios sobre la entidad
 * existente.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // Actualizar solo el monto y el estado
 * UpdateFixedExpenseRequest solicitud = UpdateFixedExpenseRequest.builder()
 *         .amount(new BigDecimal("19.99"))
 *         .status(Status.PAUSED)
 *         .build();
 *
 * // Actualizar todos los campos
 * UpdateFixedExpenseRequest solicitud = UpdateFixedExpenseRequest.builder()
 *         .name("Disney+")
 *         .frequency(Frequency.MONTHLY)
 *         .amount(new BigDecimal("10.99"))
 *         .status(Status.ACTIVE)
 *         .closingDate(LocalDate.of(2026, 6, 30))
 *         .categoryId(UUID.fromString("..."))
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class UpdateFixedExpenseRequest {

    /**
     * Nuevo nombre descriptivo que se desea asignar al gasto fijo.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza el nombre actual
     * del gasto fijo. Si es {@code null}, el nombre permanece sin cambios.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @Size(max = 20)}: el nombre no puede superar los 20 caracteres.</li>
     *   <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML, previniendo
     *       ataques de inyección de código en este campo.</li>
     * </ul>
     */
    @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
    @NoHtml
    private String name;

    /**
     * Nueva frecuencia de cobro o recurrencia que se desea asignar al gasto fijo.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la frecuencia actual
     * según el enumerado {@link Frequency}
     * (por ejemplo: diario, semanal, mensual, anual, entre otros).
     * Si es {@code null}, la frecuencia del gasto fijo permanece sin cambios.</p>
     */
    private Frequency frequency;

    /**
     * Nuevo monto monetario que se desea asignar al gasto fijo.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza el monto actual del gasto
     * recurrente. Se utiliza {@link BigDecimal} para garantizar precisión en
     * los cálculos monetarios y evitar errores de redondeo propios de tipos flotantes.
     * Si es {@code null}, el monto permanece sin cambios.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @DecimalMin("0.01")}: el monto debe ser mayor a cero,
     *       garantizando que no se registren gastos sin valor económico.</li>
     * </ul>
     */
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    /**
     * Nuevo estado operativo que se desea asignar al gasto fijo.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza el estado actual
     * del gasto recurrente según el enumerado {@link Status}
     * (por ejemplo: activo, pausado, cancelado, entre otros).
     * Si es {@code null}, el estado del gasto fijo permanece sin cambios.</p>
     */
    private Status status;

    /**
     * Nueva fecha de cierre o vencimiento que se desea asignar al gasto fijo.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la fecha de cierre actual
     * del gasto recurrente. Se representa como {@link LocalDate} sin información
     * de hora ni zona horaria, dado que la vigencia opera a nivel de día calendario.
     * Si es {@code null}, la fecha de cierre permanece sin cambios.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @Future}: la fecha debe ser estrictamente posterior a la fecha
     *       actual en el momento de la solicitud, evitando registrar vencimientos
     *       ya expirados.</li>
     * </ul>
     */
    @Future(message = "La fecha de cierre debe ser futura")
    private LocalDate nextDueDate;

    /**
     * Identificador de la nueva categoría financiera que se desea asociar
     * al gasto fijo.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la categoría actualmente
     * asociada al gasto fijo por aquella cuyo UUID corresponda al valor enviado.
     * Si es {@code null}, la categoría del gasto fijo permanece sin cambios.</p>
     */
    private UUID categoryId;
}