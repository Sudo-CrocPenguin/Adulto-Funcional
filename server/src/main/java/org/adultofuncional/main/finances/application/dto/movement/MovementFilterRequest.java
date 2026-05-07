package org.adultofuncional.main.finances.application.dto.movement;

import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovementFilterRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private MovementType movementType;
    private UUID categoryId;
    @Size(max = 100, message = "Término de búsqueda demasiado largo")
    @NoHtml
    private String searchTerm;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
