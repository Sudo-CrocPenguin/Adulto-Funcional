package org.adultofuncional.main.finances.application.dto.movement;

import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para crear un nuevo movimiento financiero.
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Objeto que encapsula los datos enviados por el cliente para registrar
 * una transacción financiera (ingreso o egreso).
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Recibe y valida la información del movimiento antes de ser procesada
 * por el caso de uso {@code CreateMovementUseCase}. Aplica protecciones
 * contra XSS y validaciones de negocio.
 * <p>
 * <strong>¿Cómo funciona?</strong><br>
 * El controlador recibe un JSON, lo deserializa a este DTO, aplica
 * validaciones automáticas (Jakarta) y pasa el objeto al caso de uso.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.movement.CreateMovementUseCase
 * @see MovementResponse
 */
@Getter
@Builder
public class CreateMovementRequest {

    /**
     * Tipo de movimiento (INGRESO o EGRESO).
     */
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType movementType;

    /**
     * Monto del movimiento. Mayor a 0.
     */
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    /**
     * Fecha en que ocurrió la transacción. No puede ser futura.
     */
    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate movementDate;

    /**
     * Descripción opcional. Sin HTML.
     */
    @Size(max = 65535, message = "La descripción es demasiado larga")
    @NoHtml
    private String description;

    /**
     * ID de la categoría asociada (opcional). Si se proporciona,
     * debe existir una categoría con ese ID.
     */
    private UUID categoryId;

    // TODO: Agregar validación de monto máximo por transacción
    // TODO: Agregar campo moneda (si se requiere internacionalización)
}
