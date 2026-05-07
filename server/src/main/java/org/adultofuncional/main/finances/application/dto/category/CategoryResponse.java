package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una categoría.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class CategoryResponse {

    private UUID id;
    private String name;
    private CategoryType type;
    private LocalDateTime createdAt;
    private boolean deleted; // soft delete flag
}
