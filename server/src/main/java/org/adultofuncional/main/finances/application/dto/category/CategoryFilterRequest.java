package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO para filtrar la lista de categorías.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class CategoryFilterRequest {

    private CategoryType type;
    private boolean includeDeleted; // por defecto false
}
