package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FixedExpenseResponse {
    private UUID id;
    private String name;
    private Frequency frequency;
    private BigDecimal amount;
    private Status status;
    private LocalDate closingDate;
    private CategoryResponse category;
}
