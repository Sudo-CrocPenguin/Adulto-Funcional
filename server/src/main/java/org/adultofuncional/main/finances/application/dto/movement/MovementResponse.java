package org.adultofuncional.main.finances.application.dto.movement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovementResponse {
    private UUID id;
    private MovementType movementType;
    private BigDecimal amount;
    private LocalDateTime registerDate;
    private String description;
    private LocalDate movementDate;
    private CategoryResponse category;
}
