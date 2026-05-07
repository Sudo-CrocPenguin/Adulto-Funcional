package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FixedExpenseFilterRequest {
    private Status status;
    private UUID categoryId;
    @Size(max = 50, message = "Término de búsqueda demasiado largo")
    @NoHtml
    private String searchTerm;
}
