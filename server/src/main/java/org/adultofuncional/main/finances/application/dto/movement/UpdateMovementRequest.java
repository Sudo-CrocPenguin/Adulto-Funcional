package org.adultofuncional.main.finances.application.dto.movement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa la solicitud de actualización
 * de un movimiento financiero existente en el sistema.
 *
 * <p>Esta clase encapsula los datos que el cliente puede enviar para modificar
 * parcialmente un movimiento financiero ya registrado. A diferencia del DTO
 * de creación, todos sus campos son opcionales, permitiendo actualizaciones
 * parciales donde solo se envían los campos que se desean modificar.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa los campos
 * modificables de un movimiento financiero, aplicando validaciones de
 * formato, seguridad y reglas de negocio únicamente sobre los campos
 * que lo requieren, sin forzar la presencia de ninguno.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Transporta desde el cliente hacia la capa de aplicación los datos
 * que se desean actualizar sobre un movimiento existente: su tipo,
 * monto, fecha, descripción y categoría asociada, o cualquier
 * combinación de ellos. Al no tener restricciones {@code @NotNull}
 * ni {@code @NotBlank}, el sistema interpreta los campos no enviados
 * como sin cambios, habilitando actualizaciones parciales (PATCH).</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * y sus campos son accesibles a través de los getters generados por {@code @Getter}.
 * Cuando el framework (Spring) recibe la solicitud, aplica las validaciones
 * activas sobre los campos proporcionados: {@code @DecimalMin} garantiza
 * un monto válido si se envía, y {@code @Size} junto con {@code @NoHtml}
 * protegen el campo de descripción. La capa de aplicación es responsable
 * de ignorar los campos nulos y aplicar solo los cambios sobre
 * la entidad existente.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // Actualizar solo el monto
 * UpdateMovementRequest solicitud = UpdateMovementRequest.builder()
 *         .amount(new BigDecimal("75.00"))
 *         .build();
 *
 * // Actualizar tipo, fecha y descripción
 * UpdateMovementRequest solicitud = UpdateMovementRequest.builder()
 *         .movementType(MovementType.INCOME)
 *         .movementDate(LocalDate.of(2025, 6, 15))
 *         .description("Pago de nómina junio")
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class UpdateMovementRequest {

    /**
     * Nuevo tipo de movimiento financiero que se desea asignar.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la clasificación actual
     * del movimiento según el enumerado {@link MovementType}, determinando
     * si corresponde a un ingreso o a un egreso en las finanzas del usuario.
     * Si es {@code null}, el tipo del movimiento permanece sin cambios.</p>
     */
    private MovementType movementType;

    /**
     * Nuevo monto monetario que se desea asignar al movimiento financiero.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza el monto actual del
     * movimiento. Se utiliza {@link BigDecimal} para garantizar precisión
     * en los cálculos monetarios y evitar errores de redondeo propios
     * de tipos flotantes. Si es {@code null}, el monto permanece sin cambios.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @DecimalMin("0.01")}: el monto debe ser mayor a cero,
     *       garantizando que no se registren movimientos sin valor económico.</li>
     * </ul>
     */
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    /**
     * Nueva fecha en la que ocurrió el movimiento financiero.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la fecha actual del
     * movimiento. Se representa como {@link LocalDate} sin información de
     * hora ni zona horaria, dado que el registro opera a nivel de día
     * calendario. Si es {@code null}, la fecha del movimiento permanece
     * sin cambios.</p>
     */
    private LocalDate movementDate;

    /**
     * Nueva descripción textual que se desea asignar al movimiento financiero.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la descripción actual
     * del movimiento, permitiendo actualizar las notas o detalles asociados
     * (por ejemplo: "Pago de nómina junio", "Compra supermercado corregida").
     * Si es {@code null}, la descripción del movimiento permanece sin cambios.</p>
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
     * Identificador de la nueva categoría financiera que se desea asociar
     * al movimiento.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la categoría actualmente
     * asociada al movimiento por aquella cuyo UUID corresponda al valor enviado.
     * Si es {@code null}, la categoría del movimiento permanece sin cambios.</p>
     */
    private UUID categoryId;
}