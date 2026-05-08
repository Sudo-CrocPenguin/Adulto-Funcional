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
 * DTO (Data Transfer Object) que representa la solicitud de creación
 * de un nuevo movimiento financiero en el sistema.
 *
 * <p>Esta clase encapsula y valida todos los datos que el cliente debe
 * enviar para registrar un movimiento financiero, ya sea un ingreso
 * o un egreso. Actúa como contrato de entrada en el caso de uso
 * de creación de movimientos.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa y valida
 * los campos requeridos para registrar un movimiento financiero,
 * aplicando restricciones de validación mediante anotaciones de
 * Bean Validation (Jakarta), seguridad de contenido y reglas de
 * negocio como montos mínimos.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Transporta desde el cliente hacia la capa de aplicación los datos
 * necesarios para registrar un movimiento: su tipo, monto, fecha,
 * descripción opcional y categoría asociada opcional. Garantiza que
 * los datos lleguen correctamente formateados y validados antes de
 * ser procesados por el sistema.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * y sus campos son accesibles a través de los getters generados por {@code @Getter}.
 * Las anotaciones de validación de Jakarta Bean Validation son procesadas
 * automáticamente por el framework (Spring) al recibir la solicitud,
 * rechazando peticiones inválidas antes de llegar al caso de uso.
 * La anotación {@code @NoHtml} aplica una validación de seguridad personalizada
 * que previene la inyección de etiquetas HTML en los campos de texto.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * CreateMovementRequest solicitud = CreateMovementRequest.builder()
 *         .movementType(MovementType.EXPENSE)
 *         .amount(new BigDecimal("50.00"))
 *         .movementDate(LocalDate.now())
 *         .description("Compra supermercado")
 *         .categoryId(UUID.fromString("..."))
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class CreateMovementRequest {

    /**
     * Tipo de movimiento financiero a registrar.
     *
     * <p>Campo obligatorio que clasifica el movimiento dentro del sistema
     * según el enumerado {@link MovementType}, determinando si corresponde
     * a un ingreso o a un egreso en las finanzas del usuario.</p>
     *
     * <p><b>Restricciones aplicadas:</b></p>
     * <ul>
     *   <li>{@code @NotNull}: el tipo de movimiento no puede ser nulo; debe
     *       proporcionarse un valor válido del enumerado {@link MovementType}.</li>
     * </ul>
     */
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType movementType;

    /**
     * Monto monetario del movimiento financiero.
     *
     * <p>Campo obligatorio que representa el valor económico del movimiento.
     * Se utiliza {@link BigDecimal} para garantizar precisión en los cálculos
     * monetarios y evitar errores de redondeo propios de tipos flotantes.</p>
     *
     * <p><b>Restricciones aplicadas:</b></p>
     * <ul>
     *   <li>{@code @NotNull}: el monto no puede ser nulo.</li>
     *   <li>{@code @DecimalMin("0.01")}: el monto debe ser mayor a cero,
     *       garantizando que no se registren movimientos sin valor económico.</li>
     * </ul>
     */
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    /**
     * Fecha en la que ocurrió el movimiento financiero.
     *
     * <p>Campo obligatorio que indica el día calendario en que se realizó
     * el ingreso o egreso. Se representa como {@link LocalDate} sin información
     * de hora ni zona horaria, dado que el registro opera a nivel de día
     * calendario.</p>
     *
     * <p><b>Restricciones aplicadas:</b></p>
     * <ul>
     *   <li>{@code @NotNull}: la fecha del movimiento no puede ser nula.</li>
     * </ul>
     */
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate movementDate;

    /**
     * Descripción textual opcional del movimiento financiero.
     *
     * <p>Campo opcional que permite al usuario agregar detalles o notas
     * sobre el movimiento registrado (por ejemplo: "Compra supermercado",
     * "Pago de servicios", "Transferencia recibida"). Si es {@code null},
     * el movimiento se registra sin descripción.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @Size(max = 65535)}: la descripción no puede superar los
     *       65,535 caracteres, límite que corresponde al tipo de dato {@code TEXT}
     *       en base de datos.</li>
     *   <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML,
     *       previniendo ataques de inyección de código en este campo.</li>
     * </ul>
     */
    @Size(max = 65535, message = "La descripción es demasiado larga")
    @NoHtml
    private String description;

    /**
     * Identificador de la categoría financiera asociada al movimiento.
     *
     * <p>Campo opcional que permite vincular el movimiento con una categoría
     * existente en el sistema para facilitar su clasificación y análisis.
     * Corresponde al UUID único de la categoría registrada.
     * Si es {@code null}, el movimiento se registra sin categoría asociada.</p>
     */
    private UUID categoryId;
}