package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para actualizar una categoría existente (PATCH).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class UpdateCategoryRequest {

    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @NoHtml
    private String name;

    private CategoryType type;
}
