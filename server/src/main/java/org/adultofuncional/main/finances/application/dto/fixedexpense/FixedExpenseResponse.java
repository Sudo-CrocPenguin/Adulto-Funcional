package org.adultofuncional.main.finances.application.dto.fixedexpense;

import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.FixedExpenseFrequency;
import org.adultofuncional.main.finances.domain.enums.FixedExpenseStatus;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de respuesta para un gasto fijo.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class FixedExpenseResponse {

    private UUID id;
    private String name;
    private FixedExpenseFrequency frequency;
    private BigDecimal amount;
    private FixedExpenseStatus status;
    private LocalDate closingDate;
    private CategoryResponse category;
}
