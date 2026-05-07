package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryFilterRequest {
    private CategoryType type;
    private boolean includeDeleted;
}
