package org.adultofuncional.main.finances.application.usecase.category;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Caso de uso: Actualizar una categoría existente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse execute(UUID categoryId, UpdateCategoryRequest request) {
        CategoryEntity entity = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));

        if (StringUtils.hasText(request.getName())) {
            boolean exists = categoryRepository.existsByNameAndTypeAndIdNot(
                    request.getName(), entity.getCategoryType(), categoryId);
            if (exists) {
                throw new BusinessException("Ya existe otra categoría con ese nombre");
            }
            entity.setCategoryName(request.getName());
        }
        if (request.getType() != null) {
            entity.setCategoryType(request.getType().name());
        }

        CategoryEntity updated = categoryRepository.save(entity);
        return CategoryResponse.builder()
                .id(updated.getCategoryId())
                .name(updated.getCategoryName())
                .type(org.adultofuncional.main.finances.domain.enums.CategoryType.valueOf(updated.getCategoryType()))
                .createdAt(updated.getCategoryCreatedAt())
                .deleted(updated.getCategoryDeletedAt() != null)
                .build();
    }
}
