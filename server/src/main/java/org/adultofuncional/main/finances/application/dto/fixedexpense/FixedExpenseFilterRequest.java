package org.adultofuncional.main.finances.application.dto.fixedexpense;

import org.adultofuncional.main.finances.domain.enums.FixedExpenseStatus;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO para filtrar gastos fijos.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class FixedExpenseFilterRequest {

    private FixedExpenseStatus status;
    private UUID categoryId;

    @Size(max = 50, message = "Término de búsqueda demasiado largo")
    @NoHtml
    private String searchTerm;
}
