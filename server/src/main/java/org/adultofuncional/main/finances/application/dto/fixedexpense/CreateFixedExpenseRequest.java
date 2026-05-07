package org.adultofuncional.main.finances.application.dto.fixedexpense;

import org.adultofuncional.main.finances.domain.enums.FixedExpenseFrequency;
import org.adultofuncional.main.finances.domain.enums.FixedExpenseStatus;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para crear un nuevo gasto fijo recurrente.
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Objeto que recibe los datos para registrar un gasto fijo
 * (suscripción, alquiler, servicio recurrente).
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Valida que el nombre, frecuencia, monto, estado y fecha de cierre
 * sean correctos antes de crear el gasto fijo.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.fixedexpense.CreateFixedExpenseUseCase
 */
@Getter
@Builder
public class CreateFixedExpenseRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
    @NoHtml
    private String name;

    @NotNull(message = "La frecuencia es obligatoria")
    private FixedExpenseFrequency frequency;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "El estado es obligatorio")
    private FixedExpenseStatus status;

    @NotNull(message = "La fecha de cierre es obligatoria")
    @Future(message = "La fecha de cierre debe ser futura")
    private LocalDate closingDate;

    private UUID categoryId;
}
