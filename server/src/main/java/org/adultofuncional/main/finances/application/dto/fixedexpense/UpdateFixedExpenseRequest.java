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

@Getter
@Builder
public class UpdateFixedExpenseRequest {
    @Size(max = 20, message = "El nombre no puede exceder 20 caracteres")
    @NoHtml
    private String name;
    private Frequency frequency;
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;
    private Status status;
    @Future(message = "La fecha de cierre debe ser futura")
    private LocalDate closingDate;
    private UUID categoryId;
}
