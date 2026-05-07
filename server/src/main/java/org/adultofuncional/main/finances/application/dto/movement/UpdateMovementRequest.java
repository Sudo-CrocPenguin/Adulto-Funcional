package org.adultofuncional.main.finances.application.dto.movement;

import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para actualizar un movimiento financiero existente (PATCH).
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Objeto que permite modificar parcial o totalmente los datos de un movimiento.
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Recibe solo los campos a actualizar, validándolos, y los pasa al caso de uso
 * {@code UpdateMovementUseCase}.
 * <p>
 * <strong>¿Cómo funciona?</strong><br>
 * El controlador recibe un JSON con los campos a modificar; todos son opcionales.
 * Si un campo está presente, se valida y se actualiza.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.movement.UpdateMovementUseCase
 */
@Getter
@Builder
public class UpdateMovementRequest {

    private MovementType movementType;

    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate movementDate;

    @Size(max = 65535, message = "La descripción es demasiado larga")
    @NoHtml
    private String description;

    private UUID categoryId;
}
