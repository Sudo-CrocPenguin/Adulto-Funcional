package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener una categoría por ID.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public CategoryResponse execute(UUID categoryId) {
        CategoryEntity entity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));
        return CategoryResponse.builder()
                .id(entity.getCategoryId())
                .name(entity.getCategoryName())
                .type(org.adultofuncional.main.finances.domain.enums.CategoryType.valueOf(entity.getCategoryType()))
                .createdAt(entity.getCategoryCreatedAt())
                .deleted(entity.getCategoryDeletedAt() != null)
                .build();
    }
}
